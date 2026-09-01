import os, runpy, math

# Build V9 and ONLY correct the blank exterior side walls.
# V10 adds real window openings on both left/right ends of the classroom wings.
runpy.run_path('tools/gen_bloco_instrucao_v9.py', run_name='__main__')

src='models/v9/BlocoInstrucao_V9.obj'
src_mtl='models/v9/BlocoInstrucao_V9.mtl'
out='models/v10'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

# Remove only the four solid classroom side-wall panels per floor.
# Keep the existing corridor-end window assemblies intact.
filtered=[]
skip=False
for line in lines:
    if line.startswith('o '):
        name=line[2:].strip()
        skip=(name.startswith('Side_L_Front_A') or
              name.startswith('Side_L_Back_A') or
              name.startswith('Side_R_Front_A') or
              name.startswith('Side_R_Back_A'))
    if not skip:
        filtered.append(line)

for i,line in enumerate(filtered):
    if line.startswith('mtllib '):
        filtered[i]='mtllib BlocoInstrucao_V10.mtl\n'
        break

vertex_count=sum(1 for l in filtered if l.startswith('v '))
append=[]

def addv(p):
    global vertex_count
    vertex_count += 1
    append.append('v %.5f %.5f %.5f\n' % p)
    return vertex_count

def use(name,mat):
    append.append(f'o {name}\n')
    append.append(f'usemtl {mat}\n')

def box(name,cx,cy,cz,sx,sy,sz,mat='WhiteBrick'):
    use(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),
         (-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    ids=[addv((cx+x,cy+y,cz+z)) for x,y,z in pts]
    for a,b,c,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        append.append(f'f {ids[a]} {ids[b]} {ids[c]} {ids[d]}\n')

def side_classroom_wall(prefix,x,y0,za,zb,side):
    """Exterior end wall of a classroom wing with two genuine window openings.
    x is +/-92; z runs along the side facade.
    """
    H=14.0
    sill=4.0
    wh=5.6
    ww=8.2
    width=zb-za
    zc=(za+zb)/2
    # two windows well separated in the side wall
    wz=[zc-width*0.24, zc+width*0.24]

    # bottom and top wall bands
    box(prefix+'_Bottom',x,y0+sill/2,zc,1,sill,width,'WhiteBrick')
    top=H-sill-wh
    box(prefix+'_Top',x,y0+sill+wh+top/2,zc,1,top,width,'WhiteBrick')

    # wall piers around openings, along Z axis
    segments=[(za,wz[0]-ww/2),(wz[0]+ww/2,wz[1]-ww/2),(wz[1]+ww/2,zb)]
    for i,(a,b) in enumerate(segments):
        if b>a:
            box(prefix+f'_Pier{i}',x,y0+sill+wh/2,(a+b)/2,1,wh,b-a,'WhiteBrick')

    # true glazing and frames through wall thickness; visible inside and outside
    outward = -1 if side=='L' else 1
    for i,z in enumerate(wz,1):
        box(prefix+f'_Window{i}_Glass',x,y0+sill+wh/2,z,0.20,wh-0.45,ww-0.50,'Glass')
        # vertical frames at z edges
        box(prefix+f'_Window{i}_FrameFront',x+outward*0.12,y0+sill+wh/2,z-ww/2,0.34,wh+0.55,0.26,'MetalDark')
        box(prefix+f'_Window{i}_FrameBack',x+outward*0.12,y0+sill+wh/2,z+ww/2,0.34,wh+0.55,0.26,'MetalDark')
        # top/bottom frames span along z
        box(prefix+f'_Window{i}_FrameTop',x+outward*0.12,y0+sill+wh,z,0.34,0.28,ww+0.55,'MetalDark')
        box(prefix+f'_Window{i}_FrameBottom',x+outward*0.12,y0+sill,z,0.34,0.28,ww+0.55,'MetalDark')
        box(prefix+f'_Window{i}_Mullion',x,y0+sill+wh/2,z,0.30,wh-0.25,0.16,'MetalDark')
        # shallow sill and sunshade on exterior face
        box(prefix+f'_Window{i}_Sill',x+outward*0.45,y0+sill-0.18,z,0.80,0.24,ww+0.80,'ConcreteLight')
        box(prefix+f'_Window{i}_Shade',x+outward*0.68,y0+sill+wh+0.45,z,1.30,0.28,ww+1.15,'ConcreteLight')

# Exact side-wall ranges inherited from V6/V9.
# Front classroom wing: z -47..-10. Back classroom wing: z 10..47.
for floor in (0,1):
    y0=floor*14.0
    side_classroom_wall(f'Side_L_Front_A{floor+1}_V10',-92,y0,-47,-10,'L')
    side_classroom_wall(f'Side_L_Back_A{floor+1}_V10', -92,y0, 10, 47,'L')
    side_classroom_wall(f'Side_R_Front_A{floor+1}_V10', 92,y0,-47,-10,'R')
    side_classroom_wall(f'Side_R_Back_A{floor+1}_V10',  92,y0, 10, 47,'R')

filtered.insert(1,'# V10: genuine side windows added to both exterior classroom end walls on both floors\n')
with open(os.path.join(out,'BlocoInstrucao_V10.obj'),'w',encoding='utf-8') as f:
    f.writelines(filtered)
    f.write('\n# --- V10 side classroom windows ---\n')
    f.writelines(append)

with open(src_mtl,'r',encoding='utf-8') as f:
    mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V10.mtl'),'w',encoding='utf-8') as f:
    f.write(mtl)

readme='''BLOCO DE INSTRUCAO V10\n\nALTERACAO UNICA DESTA VERSAO:\n- As paredes externas laterais esquerda e direita nao sao mais paredes cegas.\n- Cada ponta de ala de salas recebeu duas janelas reais por pavimento, tanto no bloco frontal quanto no traseiro.\n- Sao aberturas verdadeiras na parede, com vidro atravessando a espessura, molduras, peitoril e pequena protecao superior.\n- Janelas podem ser vistas por dentro das salas e pelo lado de fora.\n- Todo o restante do V9 foi preservado.\n'''
with open(os.path.join(out,'README_V10.txt'),'w',encoding='utf-8') as f:
    f.write(readme)

print('V10 generated with exterior side classroom windows')
