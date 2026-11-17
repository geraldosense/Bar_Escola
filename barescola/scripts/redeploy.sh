#!/usr/bin/env bash
# Recompila o BarEscola e volta a fazer o deploy no WildFly que já está a correr.
# Uso: bash scripts/redeploy.sh
set -euo pipefail
: "${WILDFLY_HOME:?Abra um novo Terminal ou corra primeiro scripts/instalar-mac.sh}"
cd "$(dirname "$0")/.."
mvn -q -B clean package
DEPLOY="$WILDFLY_HOME/standalone/deployments"
rm -f "$DEPLOY"/barescola.war.*
cp target/barescola.war "$DEPLOY/"
echo "A fazer o deploy..."
for _ in $(seq 1 60); do
  [[ -f "$DEPLOY/barescola.war.deployed" ]] && { echo "Deploy concluído: http://localhost:8080/barescola"; exit 0; }
  [[ -f "$DEPLOY/barescola.war.failed" ]] && { cat "$DEPLOY/barescola.war.failed"; exit 1; }
  sleep 2
done
echo "O WildFly não respondeu. Está a correr? (\$WILDFLY_HOME/bin/standalone.sh -c standalone-full.xml)"; exit 1
