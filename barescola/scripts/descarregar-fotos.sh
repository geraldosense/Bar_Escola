#!/usr/bin/env bash
# Guarda as fotografias dos produtos dentro do projeto (src/main/webapp/img/fotos), para a aplicação
# funcionar sem internet (ex.: Demo Day). Depois recompila e volta a fazer o deploy.
# Uso: bash scripts/descarregar-fotos.sh
set -euo pipefail
cd "$(dirname "$0")/.."
DESTINO="src/main/webapp/img/fotos"
mkdir -p "$DESTINO"
while read -r chave url; do
  [[ -z "$chave" ]] && continue
  echo "A descarregar $chave..."
  curl -fsSL -A "BarEscola/1.0 (projeto escolar)" -o "$DESTINO/$chave.jpg" "$url"
done <<'LISTA'
agua https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Evian_Label-Free_Bottle.jpg/500px-Evian_Label-Free_Bottle.jpg
sumo https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Orange_juice_1_edit1.jpg/500px-Orange_juice_1_edit1.jpg
leite-chocolate https://thumb.wikimedia.org/wikipedia/commons/thumb/2/23/Chocolate_milk.JPG/500px-Chocolate_milk.JPG
cafe https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Cup_of_espresso_02.jpg/500px-Cup_of_espresso_02.jpg
sandes https://upload.wikimedia.org/wikipedia/commons/c/c6/Ham_sandwich.jpg
tosta https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Grilled_ham_and_cheese_sandwich.jpg/500px-Grilled_ham_and_cheese_sandwich.jpg
frango https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/Chicken_sandwich.jpg/500px-Chicken_sandwich.jpg
maca https://thumb.wikimedia.org/wikipedia/commons/thumb/e/eb/Wet_apple_isolated_on_white.jpg/500px-Wet_apple_isolated_on_white.jpg
banana https://thumb.wikimedia.org/wikipedia/commons/thumb/1/10/Banana_isolated_on_white.jpg/500px-Banana_isolated_on_white.jpg
croissant https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dc/Croissants_au_beurre_%2818953292873%29.jpg/500px-Croissants_au_beurre_%2818953292873%29.jpg
pao-queijo https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/Pao_de_queijo_brasil.jpg/500px-Pao_de_queijo_brasil.jpg
rissol https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a2/Risole2.jpg/500px-Risole2.jpg
LISTA
echo "Fotografias guardadas em $DESTINO"
[[ -n "${WILDFLY_HOME:-}" ]] && bash scripts/redeploy.sh || echo "Corra scripts/redeploy.sh para publicar."
