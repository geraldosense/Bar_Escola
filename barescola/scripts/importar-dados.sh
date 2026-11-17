#!/usr/bin/env bash
# =============================================================================
#  BarEscola – importar dados exportados noutro Mac (exportar-dados.sh)
#  Substitui a base de dados e os utilizadores deste WildFly pelos do ficheiro.
#  Os dados atuais ficam guardados numa cópia de segurança.
#
#  Uso:  bash scripts/importar-dados.sh ~/Downloads/barescola-dados-AAAAMMDD-HHMM.zip
# =============================================================================
set -euo pipefail
ZIP="${1:-}"
[[ -f "$ZIP" ]] || { echo "Uso: bash scripts/importar-dados.sh <ficheiro barescola-dados-*.zip>"; exit 1; }
WILDFLY_HOME="${WILDFLY_HOME:-$HOME/Developer/wildfly}"
ST="$WILDFLY_HOME/standalone"
LOGS="$HOME/Developer/logs"; mkdir -p "$LOGS"
[[ -x "$WILDFLY_HOME/bin/standalone.sh" ]] || { echo "ERRO: WildFly não encontrado. Corra primeiro scripts/instalar-mac.sh"; exit 1; }

# Confirmar que é um ficheiro de dados do BarEscola
unzip -l "$ZIP" | grep -q "data/barescola/" || { echo "ERRO: $ZIP não parece uma exportação do BarEscola"; exit 1; }

if curl -s -o /dev/null http://localhost:8080; then
  echo ">> A parar o WildFly..."
  "$WILDFLY_HOME/bin/jboss-cli.sh" --connect --command=":shutdown" >/dev/null
  for _ in $(seq 1 30); do curl -s -o /dev/null http://localhost:8080 || break; sleep 1; done
fi

COPIA="$ST/backup-$(date +%Y%m%d-%H%M%S)"
echo ">> Cópia de segurança dos dados atuais em $COPIA"
mkdir -p "$COPIA/configuration"
[[ -d "$ST/data/barescola" ]] && mv "$ST/data/barescola" "$COPIA/barescola-data"
cp "$ST/configuration/application-users.properties" "$ST/configuration/application-roles.properties" "$COPIA/configuration/" 2>/dev/null || true

echo ">> A importar $ZIP"
unzip -q -o "$ZIP" -d "$ST"

echo ">> A ligar o WildFly..."
nohup "$WILDFLY_HOME/bin/standalone.sh" -c standalone-full.xml > "$LOGS/wildfly.log" 2>&1 &
for _ in $(seq 1 90); do
  curl -s http://localhost:8080/barescola/api/estado | grep -q '"OK"' && break
  sleep 2
done

if curl -s http://localhost:8080/barescola/api/estado | grep -q '"OK"'; then
  echo
  echo "Importação concluída. Dados iguais aos do Mac de origem: http://localhost:8080/barescola"
  open "http://localhost:8080/barescola" 2>/dev/null || true
else
  echo "O WildFly arrancou mas a aplicação não respondeu. Veja: tail -50 $LOGS/wildfly.log"
  echo "Se não houver barescola.war em $ST/deployments, corra: bash scripts/redeploy.sh"
fi
