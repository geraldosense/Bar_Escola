import sys
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.recordingPen import RecordingPen

DEST = sys.argv[1]
NAVY = '#1b2a4a'; AMARELO = '#ffc629'
f = TTFont('/usr/share/fonts/truetype/dejavu/DejaVuSansCondensed-Bold.ttf')
gs = f.getGlyphSet(); cmap = f.getBestCmap(); upm = f['head'].unitsPerEm; hmtx = f['hmtx']
fr = TTFont('/usr/share/fonts/truetype/dejavu/DejaVuSansCondensed.ttf')
gsr = fr.getGlyphSet(); cmr = fr.getBestCmap(); hr = fr['hmtx']

def bounds(g):
    bp = BoundsPen(gs); gs[g].draw(bp); return bp.bounds

def espessura_base_E():
    """Altura da barra inferior do E (menor y acima de 0 entre os pontos do contorno)."""
    rp = RecordingPen(); gs[cmap[ord('E')]].draw(rp)
    ys = sorted({round(pt[1]) for op, pts in rp.value for pt in pts})
    return [y for y in ys if y > 0][0]

CAP = 1493; ARM = 291; YB = 633 + ARM / 2   # medidas do E da DejaVu Sans Condensed Bold

def ligadura_es(ox, base, sc, cor, Ws=900, gap=70):
    """Monograma ES: o braço do meio do E continua e torna-se a espinha do S (uma só peça, sem costuras)."""
    def P(x, y): return f'{ox + x * sc:.2f},{base - y * sc:.2f}'
    # E sem o braço do meio (o braço é desenhado como início do traço do S)
    e = (f'M{P(169,0)} L{P(1124,0)} L{P(1124,ARM)} L{P(516,ARM)} L{P(516,1202)} L{P(1104,1202)} '
         f'L{P(1104,CAP)} L{P(169,CAP)} Z')
    w = ARM; hw = w / 2; xs = 1104 + gap; R = Ws - w
    xl, xr, xm = xs + hw, xs + Ws - hw, xs + Ws / 2
    sup = (f'M{P(xr, CAP-400)} C{P(xr, CAP-hw)} {P(xm+R*0.25, CAP-hw)} {P(xm, CAP-hw)} '
           f'C{P(xl, CAP-hw)} {P(xl, CAP-hw-120)} {P(xl, CAP-hw-200)} '
           f'C{P(xl, YB+170)} {P(xl+200, YB)} {P(xm+40, YB)}')
    inf = (f'M{P(430, YB)} L{P(xm+40, YB)} '
           f'C{P(xr, YB-10)} {P(xr, YB-150)} {P(xr, YB-300)} '
           f'C{P(xr, hw+120)} {P(xm+R*0.3, ARM/2)} {P(xm, ARM/2)} '
           f'C{P(xl+80, ARM/2)} {P(xl, ARM/2+120)} {P(xl, 420)}')
    sw = w * sc
    svg = (f'<path d="{e}" fill="{cor}"/>'
           f'<path d="{sup}" fill="none" stroke="{cor}" stroke-width="{sw:.2f}"/>'
           f'<path d="{inf}" fill="none" stroke="{cor}" stroke-width="{sw:.2f}"/>')
    return svg, xs + Ws + 60

def nome(tam, x0, base, cor_texto, tracking=-0.01):
    s = tam / upm; partes = []; x = 0
    for i, ch in enumerate('Bar'):
        g = cmap[ord(ch)]; pen = SVGPathPen(gs)
        gs[g].draw(TransformPen(pen, (s, 0, 0, -s, x0 + x * s, base)))
        partes.append(f'<path d="{pen.getCommands()}" fill="{cor_texto}"/>')
        x += hmtx[g][0] + tracking * upm
    lig, avanco = ligadura_es(x0 + x * s, base, s, AMARELO)
    partes.append(lig); x += avanco + tracking * upm
    for ch in 'cola':
        g = cmap[ord(ch)]; pen = SVGPathPen(gs)
        gs[g].draw(TransformPen(pen, (s, 0, 0, -s, x0 + x * s, base)))
        partes.append(f'<path d="{pen.getCommands()}" fill="{cor_texto}"/>')
        x += hmtx[g][0] + tracking * upm
    return ''.join(partes), x * s

def frase(txt, tam, x0, base):
    sc = tam / fr['head'].unitsPerEm; pen = SVGPathPen(gsr); x = 0
    for ch in txt:
        g = cmr[ord(ch)]; gsr[g].draw(TransformPen(pen, (sc, 0, 0, -sc, x0 + x * sc, base))); x += hr[g][0]
    return pen.getCommands(), x * sc

def simbolo(x, y, h, papel=NAVY, fundo=AMARELO):
    w = h * 1.25; r = h * 0.14; n = h * 0.15; cx, cy = x + w / 2, y + h / 2
    talao = (f'M{x+r},{y} H{x+w-r} A{r},{r} 0 0 1 {x+w},{y+r} V{cy-n} A{n},{n} 0 0 0 {x+w},{cy+n} V{y+h-r} '
             f'A{r},{r} 0 0 1 {x+w-r},{y+h} H{x+r} A{r},{r} 0 0 1 {x},{y+h-r} V{cy+n} A{n},{n} 0 0 0 {x},{cy-n} V{y+r} '
             f'A{r},{r} 0 0 1 {x+r},{y} Z')
    R = h * 0.29; sw = h * 0.075
    return (f'<path d="{talao}" fill="{fundo}"/><circle cx="{cx}" cy="{cy}" r="{R}" fill="none" stroke="{papel}" stroke-width="{sw}"/>'
            f'<path d="M{cx},{cy} V{cy-R} A{R},{R} 0 0 1 {cx+R},{cy} Z" fill="{papel}"/><circle cx="{cx}" cy="{cy}" r="{h*0.055}" fill="{papel}"/>'), w

def gravar(nomef, conteudo, w, h):
    open(f'{DEST}/{nomef}', 'w').write(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w:.1f} {h:.1f}" width="{w:.0f}" height="{h:.0f}" role="img" aria-label="BarEscola"><title>BarEscola</title>{conteudo}</svg>\n')

s, w = simbolo(2, 6, 48); gravar('simbolo.svg', s, w + 4, 60)
H = 48; s, w = simbolo(0, 4, H)
for nomef, cor in (('logo.svg', NAVY), ('logo-branco.svg', '#ffffff')):
    d, tw = nome(46, w + 14, 4 + H / 2 + 16.5, cor)
    gravar(nomef, s + d, w + 14 + tw + 2, H + 8)
d, tw = nome(46, w + 14, 4 + H / 2 + 16.5, NAVY)
d2, tw2 = frase('Encomenda na aula. Levanta no intervalo.', 15, w + 15, 4 + H + 20)
gravar('logo-frase.svg', s + d + f'<path d="{d2}" fill="#5a6272"/>', max(w + 14 + tw, w + 15 + tw2) + 2, H + 30)
print('ok, espessura E =', espessura_base_E())
