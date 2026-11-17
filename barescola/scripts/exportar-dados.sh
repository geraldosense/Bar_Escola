#!/usr/bin/env bash
# =============================================================================
#  BarEscola – exportar os dados deste Mac (base de dados H2 + utilizadores)
#  Gera ~/Desktop/barescola-dados-<data>.zip para levar para outro Mac
#  (AirDrop, pen, email...). No outro Mac: bash scripts/importar-dados.sh <zip>
#
#  Uso:  bash scripts/exportar-dados.sh
#  O WildFly é parado durante a cópia (para a base de dados ficar consistente)
#  e volta a ser ligado no fim.
# =============================================================================
set -euo pipefail
WILDFLY_HOME="${WILDFLY_HOME:-$HOME/Developer/wildfly}"
ST="$WILDFLY_HOME/standalone"
LOGS="$HOME/Developer/logs"; mkdir -p "$LOGS"
DESTINO="$HOME/Desktop/barescola-dados-$(date +%Y%m%d-%H%M).zip"

[[ -d "$ST/data/barescola" ]] || { echo "ERRO: não encontrei a base de dados em $ST/data/barescola"; exit 1; }

estava_ligado=false
if curl -s -o /dev/null http://localhost:8080; then
  estava_ligado=true
  echo ">> A parar o WildFly..."
  "$WILDFLY_HOME/bin/jboss-cli.sh" --connect --command=":shutdown" >/dev/null
  for _ in $(seq 1 30); do curl -s -o /dev/null http://localhost:8080 || break; sleep 1; done
fi

echo ">> A criar $DESTINO"
cd "$ST"
zip -q -r "$DESTINO" data/barescola \
  configuration/application-users.properties configuration/application-roles.properties

if $estava_ligado; then
  echo ">> A voltar a ligar o WildFly..."
  nohup "$WILDFLY_HOME/bin/standalone.sh" -c standalone-full.xml > "$LOGS/wildfly.log" 2>&1 &
fi

echo
echo "Exportação concluída: $DESTINO"
echo "Envie este ficheiro para o outro Mac e corra lá:  bash scripts/importar-dados.sh <ficheiro.zip>"
