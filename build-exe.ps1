# build-exe.ps1
# Gera instalador Windows com jpackage usando o JAR correto automaticamente.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Biblioteca Marcus Felix - Build EXE" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1) Compilar com Maven
Write-Host "1) mvn clean package..." -ForegroundColor Yellow
mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro na compilação Maven (mvn clean package). Corrija e tente novamente." -ForegroundColor Red
    exit 1
}
Write-Host "✅ Maven compilou com sucesso.`n" -ForegroundColor Green

# 2) Encontrar JAR apropriado em target/
Write-Host "2) Procurando JAR em target/..." -ForegroundColor Yellow
$targetDir = Join-Path (Get-Location) "target"
if (-not (Test-Path $targetDir)) {
    Write-Host "❌ Pasta target/ não encontrada. Execute mvn package." -ForegroundColor Red
    exit 1
}

# pega JARs, ignora backups e fontes
$jars = Get-ChildItem $targetDir -Filter *.jar -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch 'original|sources|javadoc' }

if (-not $jars -or $jars.Count -eq 0) {
    # fallback: pega qualquer jar
    $jars = Get-ChildItem $targetDir -Filter *.jar -File -ErrorAction SilentlyContinue
}

if (-not $jars -or $jars.Count -eq 0) {
    Write-Host "❌ Nenhum JAR encontrado em target/. Verifique o build." -ForegroundColor Red
    exit 1
}

# escolher o maior JAR (provável uber-jar)
$jarFile = $jars | Sort-Object Length -Descending | Select-Object -First 1
Write-Host ("✅ JAR selecionado: {0} ({1:N2} MB)" -f $jarFile.Name, ($jarFile.Length/1MB)) -ForegroundColor Green

# 3) Verificar jpackage
Write-Host "`n3) Verificando jpackage..." -ForegroundColor Yellow
$jpackageCmd = Get-Command jpackage -ErrorAction SilentlyContinue
if (-not $jpackageCmd) {
    Write-Host "❌ 'jpackage' não encontrado no PATH. Garanta que você esteja usando JDK 21+ e que 'jpackage' esteja disponível." -ForegroundColor Red
    Write-Host "Exemplo: abra um terminal com o JDK 21 (bin folder) no PATH e rode este script novamente." -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ jpackage encontrado: $($jpackageCmd.Source)`n" -ForegroundColor Green

# 4) Checar WiX (light.exe / candle.exe), tentar detectar instalação 3.x comum e adicionar ao PATH temporariamente
Write-Host "4) Verificando WiX (light.exe / candle.exe)..." -ForegroundColor Yellow
$hasLight = (Get-Command light.exe -ErrorAction SilentlyContinue) -ne $null
$hasCandle = (Get-Command candle.exe -ErrorAction SilentlyContinue) -ne $null

if ($hasLight -and $hasCandle) {
    Write-Host "✅ WiX já disponível no PATH." -ForegroundColor Green
} else {
    Write-Host "⚠️ WiX não encontrado no PATH. Tentando localizar instalações comuns (ex: WiX 3.11)..." -ForegroundColor Yellow

    $possibleRoots = @(
        'C:\Program Files (x86)\WiX Toolset*',
        'C:\Program Files\WiX Toolset*',
        'C:\Program Files (x86)\WiX*',
        'C:\Program Files\WiX*'
    )

    $found = $null
    foreach ($pattern in $possibleRoots) {
        $candidates = Get-ChildItem -Path $pattern -Directory -ErrorAction SilentlyContinue
        foreach ($c in $candidates) {
            $bin = Join-Path $c.FullName 'bin'
            if (Test-Path (Join-Path $bin 'light.exe') -PathType Leaf -ErrorAction SilentlyContinue -ErrorVariable +err) {
                $found = $bin
                break
            }
            if (Test-Path (Join-Path $bin 'candle.exe') -PathType Leaf -ErrorAction SilentlyContinue) {
                $found = $bin
                break
            }
        }
        if ($found) { break }
    }

    if ($found) {
        Write-Host "✅ Encontrado WiX em: $found" -ForegroundColor Green
        # adicionar temporariamente ao PATH do processo atual
        $env:PATH = "$found;$env:PATH"
        Write-Host "Adicionado '$found' ao PATH desta sessão. (Para persistir, adicione ao PATH do usuário via GUI ou setx)" -ForegroundColor Green

        # tentar também gravar no PATH do usuário (setx)
        try {
            $currentUserPath = [Environment]::GetEnvironmentVariable('PATH','User')
            if ($currentUserPath -notlike "*$found*") {
                setx PATH ("$currentUserPath;$found") | Out-Null
                Write-Host "Tentativa de adicionar ao PATH do usuário concluída (use nova janela de PowerShell para ver efeito persistente)." -ForegroundColor Green
            } else {
                Write-Host "WiX já consta no PATH do usuário." -ForegroundColor Green
            }
        } catch {
            Write-Host "⚠️ Falha ao gravar PATH do usuário via setx: $($_.Exception.Message)" -ForegroundColor Yellow
            Write-Host "Adicione manualmente '$found' ao PATH do usuário se desejar persistência." -ForegroundColor Yellow
        }

        # reavaliar
        $hasLight = (Get-Command light.exe -ErrorAction SilentlyContinue) -ne $null
        $hasCandle = (Get-Command candle.exe -ErrorAction SilentlyContinue) -ne $null
    } else {
        Write-Host "⚠️ Não encontrei WiX 3.x automaticamente. Se você quiser criar instaladores .exe/.msi, instale WiX Toolset 3.11 e adicione o folder '...\\WiX Toolset v3.11\\bin' ao PATH." -ForegroundColor Yellow
        Write-Host "Continuando: se for apenas gerar um runtime-less installer ou testar, jpackage pode falhar sem WiX." -ForegroundColor Yellow
    }
}

# 5) Preparar pasta dist
Write-Host "`n5) Preparando pasta de saída 'dist'..." -ForegroundColor Yellow
$dist = Join-Path (Get-Location) "dist"
if (-not (Test-Path $dist)) { New-Item -ItemType Directory -Path $dist | Out-Null }

# 6) Rodar jpackage
Write-Host "`n6) Executando jpackage..." -ForegroundColor Yellow

# montar argumentos do jpackage
$jpackageArgs = @(
    '--input', 'target',
    '--name', 'BibliotecaMarcusFelix',
    '--main-jar', $jarFile.Name,
    '--main-class', 'com.biblioteca.AppLauncher',
    '--type', 'exe',
    '--dest', 'dist',
    '--win-menu',
    '--win-shortcut',
    '--win-console'
)

# Executa jpackage
Write-Host "jpackage $($jpackageArgs -join ' ')" -ForegroundColor Gray
& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ jpackage retornou erro (exit code $LASTEXITCODE)." -ForegroundColor Red
    Write-Host "Verifique:" -ForegroundColor Yellow
    Write-Host "- se WiX (candle.exe/light.exe) está disponível no PATH (necessário para .exe/.msi no Windows)." -ForegroundColor Yellow
    Write-Host "- se a versão do JDK/jpackage é compatível (JDK 21 para JavaFX 21)." -ForegroundColor Yellow
    Write-Host "- se há mensagens de erro detalhadas acima no console do jpackage." -ForegroundColor Yellow
    exit $LASTEXITCODE
}

Write-Host "`n✅ jpackage concluiu com sucesso. Arquivos gerados em: $dist" -ForegroundColor Green
Get-ChildItem $dist | Format-Table Name,Length -AutoSize
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BUILD CONCLUÍDO" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan