#!/usr/bin/env bash
# =============================================================================
#  BarEscola – instalação completa do ambiente de desenvolvimento no macOS
# -----------------------------------------------------------------------------
#  Instala (sem precisar de palavra-passe de administrador):
#    • JDK 21 (Eclipse Temurin)   → ~/Library/Java/JavaVirtualMachines/temurin-21.jdk
#    • Apache Maven 3.9           → ~/Developer/apache-maven-<versão>   (+ atalho ~/Developer/maven)
#    • WildFly EE 10 (Jakarta EE 10) → ~/Developer/wildfly-ee-10-<versão> (+ atalho ~/Developer/wildfly)
#  Depois configura:
#    • variáveis JAVA_HOME, MAVEN_HOME, WILDFLY_HOME e PATH (~/.bash_profile e ~/.zshrc)
#    • datasource H2 + utilizadores de teste no WildFly (scripts/setup-wildfly.sh)
#  E, por fim, compila o projeto, arranca o WildFly, faz o deploy e testa /api/estado.
#
#  Uso:   bash scripts/instalar-mac.sh
#  Pode ser corrido mais do que uma vez: o que já estiver instalado é reaproveitado.
# =============================================================================
set -euo pipefail

MAVEN_VERSAO="3.9.11"
WILDFLY_VERSOES=("41.0.1.Final" "41.0.0.Final")   # tenta a mais recente primeiro

DEV="$HOME/Developer"
JVM_DIR="$HOME/Library/Java/JavaVirtualMachines"
JDK_DIR="$JVM_DIR/temurin-21.jdk"
LOGS="$DEV/logs"
PROJETO="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

verde() { printf "\033[32m%s\033[0m\n" "$*"; }
azul()  { printf "\n\033[1;34m==> %s\033[0m\n" "$*"; }
erro()  { printf "\033[31mERRO: %s\033[0m\n" "$*" >&2; exit 1; }

[[ "$(uname -s)" == "Darwin" ]] || erro "Este script é para macOS."
case "$(uname -m)" in
  x86_64) ARCH="x64" ;;
  arm64)  ARCH="aarch64" ;;
  *) erro "Arquitetura não suportada: $(uname -m)" ;;
esac
mkdir -p "$DEV" "$JVM_DIR" "$LOGS"

# -----------------------------------------------------------------------------
azul "1/7  JDK 21 (Eclipse Temurin, $ARCH)"
if [[ -x "$JDK_DIR/Contents/Home/bin/java" ]]; then
  verde "Já instalado em $JDK_DIR"
else
  curl -fL --progress-bar -o "$TMP/jdk.tar.gz" \
    "https://api.adoptium.net/v3/binary/latest/21/ga/mac/$ARCH/jdk/hotspot/normal/eclipse"
  mkdir -p "$TMP/jdk" && tar -xzf "$TMP/jdk.tar.gz" -C "$TMP/jdk"
  mv "$TMP"/jdk/jdk-21* "$JDK_DIR"
  verde "Instalado em $JDK_DIR"
fi
export JAVA_HOME="$JDK_DIR/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
java -version 2>&1 | head -1

# -----------------------------------------------------------------------------
azul "2/7  Apache Maven $MAVEN_VERSAO"
MAVEN_DIR="$DEV/apache-maven-$MAVEN_VERSAO"
if [[ -x "$MAVEN_DIR/bin/mvn" ]]; then
  verde "Já instalado em $MAVEN_DIR"
else
  curl -fL --progress-bar -o "$TMP/maven.tar.gz" \
    "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSAO/binaries/apache-maven-$MAVEN_VERSAO-bin.tar.gz"
  tar -xzf "$TMP/maven.tar.gz" -C "$DEV"
  verde "Instalado em $MAVEN_DIR"
fi
ln -sfn "$MAVEN_DIR" "$DEV/maven"
export MAVEN_HOME="$DEV/maven"
export PATH="$MAVEN_HOME/bin:$PATH"
mvn -v | head -1

# -----------------------------------------------------------------------------
azul "3/7  WildFly EE 10 (Jakarta EE 10)"
WILDFLY_DIR=""
for v in "${WILDFLY_VERSOES[@]}"; do
  if [[ -x "$DEV/wildfly-ee-10-$v/bin/standalone.sh" ]]; then
    WILDFLY_DIR="$DEV/wildfly-ee-10-$v"; verde "Já instalado em $WILDFLY_DIR"; break
  fi
done
if [[ -z "$WILDFLY_DIR" ]]; then
  for v in "${WILDFLY_VERSOES[@]}"; do
    url="https://github.com/wildfly/wildfly/releases/download/$v/wildfly-ee-10-$v.tar.gz"
    echo "A descarregar $url"
    if curl -fL --progress-bar -o "$TMP/wildfly.tar.gz" "$url"; then
      tar -xzf "$TMP/wildfly.tar.gz" -C "$DEV"
      WILDFLY_DIR="$DEV/wildfly-ee-10-$v"
      verde "Instalado em $WILDFLY_DIR"
      break
    fi
    echo "Versão $v indisponível, a tentar a seguinte..."
  done
fi
[[ -n "$WILDFLY_DIR" ]] || erro "Não foi possível descarregar o WildFly EE 10. Descarregue-o em https://wildfly.org/downloads para ~/Developer e volte a correr o script."
ln -sfn "$WILDFLY_DIR" "$DEV/wildfly"
export WILDFLY_HOME="$DEV/wildfly"

# -----------------------------------------------------------------------------
azul "4/7  Variáveis de ambiente"
BLOCO_INICIO="# >>> BarEscola – ambiente Java >>>"
BLOCO_FIM="# <<< BarEscola – ambiente Java <<<"
BLOCO="$BLOCO_INICIO
export JAVA_HOME=\"\$HOME/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home\"
export MAVEN_HOME=\"\$HOME/Developer/maven\"
export WILDFLY_HOME=\"\$HOME/Developer/wildfly\"
export PATH=\"\$JAVA_HOME/bin:\$MAVEN_HOME/bin:\$WILDFLY_HOME/bin:\$PATH\"
$BLOCO_FIM"
for perfil in "$HOME/.bash_profile" "$HOME/.zshrc"; do
  touch "$perfil"
  if grep -qF "$BLOCO_INICIO" "$perfil"; then
    # substitui o bloco antigo
    awk -v ini="$BLOCO_INICIO" -v fim="$BLOCO_FIM" '
      $0==ini {skip=1; next} $0==fim {skip=0; next} !skip' "$perfil" > "$TMP/perfil" && cat "$TMP/perfil" > "$perfil"
  fi
  printf "\n%s\n" "$BLOCO" >> "$perfil"
  verde "Atualizado: $perfil"
done

# -----------------------------------------------------------------------------
azul "5/7  Configurar o WildFly (datasource H2 + utilizadores)"
if pgrep -f "jboss-modules.jar" >/dev/null; then
  echo "WildFly a correr – a parar para configurar..."
  "$WILDFLY_HOME/bin/jboss-cli.sh" --connect --command=":shutdown" >/dev/null 2>&1 || true
  sleep 5
fi
bash "$PROJETO/scripts/setup-wildfly.sh"

# -----------------------------------------------------------------------------
azul "6/7  Compilar o projeto (mvn package)"
cd "$PROJETO"
mvn -q -B clean package
verde "Gerado: $PROJETO/target/barescola.war"

# -----------------------------------------------------------------------------
azul "7/7  Arrancar o WildFly e fazer o deploy"
nohup "$WILDFLY_HOME/bin/standalone.sh" -c standalone-full.xml > "$LOGS/wildfly.log" 2>&1 &
echo "WildFly a arrancar (log: $LOGS/wildfly.log)..."
for _ in $(seq 1 90); do
  curl -s -o /dev/null http://localhost:8080 && break
  sleep 2
done

DEPLOY="$WILDFLY_HOME/standalone/deployments"
rm -f "$DEPLOY"/barescola.war.*
cp target/barescola.war "$DEPLOY/"
echo "A fazer o deploy..."
for _ in $(seq 1 90); do
  [[ -f "$DEPLOY/barescola.war.deployed" || -f "$DEPLOY/barescola.war.failed" ]] && break
  sleep 2
done

if [[ -f "$DEPLOY/barescola.war.failed" ]]; then
  echo
  cat "$DEPLOY/barescola.war.failed"
  erro "O deploy falhou. Veja o log em $LOGS/wildfly.log e envie o erro."
fi

ESTADO="$(curl -s http://localhost:8080/barescola/api/estado || true)"
echo
echo "GET /barescola/api/estado → $ESTADO"
echo
verde "============================================================"
verde " Tudo instalado e a correr!"
verde "   Aplicação:   http://localhost:8080/barescola"
verde "   Consola WF:  http://localhost:9990"
verde "   Log:         $LOGS/wildfly.log"
verde ""
verde " Abra um NOVO Terminal (para carregar as variáveis) e use:"
verde "   java -version · mvn -v · \$WILDFLY_HOME"
verde " Parar o WildFly:  \$WILDFLY_HOME/bin/jboss-cli.sh --connect command=:shutdown"
verde "============================================================"
open "http://localhost:8080/barescola" 2>/dev/null || true
