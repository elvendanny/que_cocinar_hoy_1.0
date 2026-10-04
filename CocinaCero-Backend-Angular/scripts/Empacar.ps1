$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$destino = Join-Path (Split-Path -Parent $raiz) 'CocinaCero-Backend-Angular-Entrega.zip'
$temp = Join-Path ([IO.Path]::GetTempPath()) ('cocina-d3-' + [Guid]::NewGuid().ToString('N'))
$salida = Join-Path $temp 'CocinaCero-Backend-Angular'
New-Item -ItemType Directory -Path $salida | Out-Null
try {
    Get-ChildItem -LiteralPath $raiz -Recurse -File -Force | ForEach-Object {
        $rel = $_.FullName.Substring($raiz.Length + 1)
        if ($rel -match '(^|[\/])(target|node_modules|datos|configuracion-local|\.git)([\/]|$)' -or $_.Extension -eq '.log' -or $_.Name -eq '.env') { return }
        $nuevo = Join-Path $salida $rel
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $nuevo) | Out-Null
        Copy-Item -LiteralPath $_.FullName -Destination $nuevo
    }
    New-Item -ItemType Directory -Force -Path (Join-Path $salida 'configuracion-local') | Out-Null
    Copy-Item -LiteralPath (Join-Path $raiz 'configuracion-local/LEEME.txt') -Destination (Join-Path $salida 'configuracion-local/LEEME.txt')
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    if (Test-Path -LiteralPath $destino) { Remove-Item -LiteralPath $destino -Force }
    [IO.Compression.ZipFile]::CreateFromDirectory($temp, $destino)
    Write-Host "Entrega creada: $destino"
} finally { Remove-Item -LiteralPath $temp -Recurse -Force }
