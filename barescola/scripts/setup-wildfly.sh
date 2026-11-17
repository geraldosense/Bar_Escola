#!/usr/bin/env bash
# Configura o WildFly para o BarEscola: datasource H2 + utilizadores de teste com papéis.
# Uso: WILDFLY_HOME=/caminho/para/wildfly ./scripts/setup-wildfly.sh   (com o servidor parado)
set -euo pipefail

if [[ -z "${WILDFLY_HOME:-}" ]]; then
  echo "Defina a variável WILDFLY_HOME (ex.: export WILDFLY_HOME=~/wildfly-35.0.1.Final)" >&2
  exit 1
fi

DIR="$(cd "$(dirname "$0")" && pwd)"

echo ">> A criar datasource BarEscolaDS..."
"$WILDFLY_HOME/bin/jboss-cli.sh" --file="$DIR/configurar-wildfly.cli"

echo ">> A criar utilizadores de teste (ApplicationRealm)..."
add() { "$WILDFLY_HOME/bin/add-user.sh" -a -s -u "$1" -p "$2" -g "$3"; }
add "aluno1@barescola.pt" "Aluno#2026"  "ALUNO"
add "aluno2@barescola.pt" "Aluno#2026"  "ALUNO"
add "aluno3@barescola.pt" "Aluno#2026"  "ALUNO"
add "balcao@barescola.pt" "Balcao#2026" "FUNCIONARIO"
add "gestor@barescola.pt" "Gestor#2026" "GESTOR"

echo ">> Concluído. Inicie com: \$WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml"
