$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Path ./build -Force | Out-Null

Remove-Item -Path ./build/*.jar -Force -ErrorAction SilentlyContinue

if (-Not (Test-Path "./versions")) {
    Write-Error "Missing ./versions directory"
    exit 1
}
Set-Location ./versions

Get-ChildItem -Directory | ForEach-Object {
    $versionDir = $_.Name
    $jarDir = Join-Path $versionDir "build/libs"

    if (Test-Path $jarDir) {
        $jars = Get-ChildItem -Path $jarDir -Filter "chatclef-$versionDir-*.jar" | Where-Object {
            $_.Name -match "^chatclef-$versionDir-(\d+\.\d+\.\d+)\.jar$"
        }

        if ($jars.Count -gt 0) {
            $latestJar = $jars | Sort-Object { [Version]($_.Name -replace ".*-$versionDir-", "").Replace(".jar", "") } | Select-Object -Last 1
            Write-Host "Copying: $($latestJar.Name)"
            Copy-Item -Path $latestJar.FullName -Destination ../build/
        } else {
            Write-Host "No valid JARs found in $versionDir"
        }
    } else {
        Write-Host "No build/libs directory in $versionDir"
    }
}

# go back to original directory
Set-Location ..
