# Configura o WildFly para o BarEscola: datasource H2 + utilizadores de teste com papéis.
# Uso (PowerShell, com o servidor parado):
#   $env:WILDFLY_HOME = "C:\wildfly-35.0.1.Final"
#   powershell -ExecutionPolicy Bypass -File scripts\setup-wildfly.ps1
$ErrorActionPreference = "Stop"

if (-not $env:WILDFLY_HOME) {
    Write-Error "Defina a variavel WILDFLY_HOME (ex.: `$env:WILDFLY_HOME = 'C:\wildfly-35.0.1.Final')"
}

$bin = Join-Path $env:WILDFLY_HOME "bin"
$cli = Join-Path $PSScriptRoot "configurar-wildfly.cli"

Write-Host ">> A criar datasource BarEscolaDS..."
& (Join-Path $bin "jboss-cli.bat") "--file=$cli"

Write-Host ">> A criar utilizadores de teste (ApplicationRealm)..."
$users = @(
    @("aluno1@barescola.pt", "Aluno#2026",  "ALUNO"),
    @("aluno2@barescola.pt", "Aluno#2026",  "ALUNO"),
    @("aluno3@barescola.pt", "Aluno#2026",  "ALUNO"),
    @("balcao@barescola.pt", "Balcao#2026", "FUNCIONARIO"),
    @("gestor@barescola.pt", "Gestor#2026", "GESTOR")
)
foreach ($u in $users) {
    & (Join-Path $bin "add-user.bat") -a -s -u $u[0] -p $u[1] -g $u[2]
}

Write-Host ">> Concluido. Inicie com: %WILDFLY_HOME%\bin\standalone.bat -c standalone-full.xml"
