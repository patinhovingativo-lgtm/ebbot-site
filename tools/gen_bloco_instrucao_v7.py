import os, runpy, math, shutil

# Build V6 first, then patch its OBJ into V7.
runpy.run_path('tools/gen_bloco_instrucao_v6.py', run_name='__main__')

src='models/v6/BlocoInstrucao_V6.obj'
src_mtl='models/v6/BlocoInstrucao_V6.mtl'
out='models/v7'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

# Remove ONLY classroom door leaves/frames/vision glass/plaques.
# Keep WallL, WallR and Header so the doorway remains an OPENING near the corner.
filtered=[]
skip=False
for line in lines:
    if line.startswith('o '):
        name=line[2:].strip()
        is_classroom=('FrontDoor' in name or 'BackDoor' in name)
        decorative=any(tok in name for tok in ('_Door','_Vision','_Frame','_Plaque'))
        skip=is_classroom and decorative
    if not skip:
        filtered.append(line)

# Rename MTL reference/header.
for i,line in enumerate(filtered):
    if line.startswith('mtllib '):
        filtered[i]='mtllib BlocoInstrucao_V7.mtl\n'
        break

# Count existing vertices so appended faces use correct indices.
vertex_count=sum(1 for l in filtered if l.startswith('v '))
append=[]

def addv(p):
    global vertex_count
    vertex_count+=1
    append.append('v %.5f %.5f %.5f\n' % p)
    return vertex_count

def add_obj(name,mat):
    append.append(f'o {name}\n'); append.append(f'usemtl {mat}\n')

def box(name,cx,cy,cz,sx,sy,sz,mat='ConcreteLight',rz=0):
    add_obj(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),(-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    c,s=math.cos(rz),math.sin(rz)
    ids=[]
    for x,y,z in pts:
        x,y=x*c-y*s,x*s+y*c
        ids.append(addv((cx+x,cy+y,cz+z)))
    for a,b,c1,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        append.append(f'f {ids[a]} {ids[b]} {ids[c1]} {ids[d]}\n')

def tri_prism_z(name,z1,z2,p1,p2,p3,mat='WhiteBrick'):
    add_obj(name,mat)
    ids=[]
    for z in (z1,z2):
        for x,y in (p1,p2,p3): ids.append(addv((x,y,z)))
    # closed prism, both triangle faces + sides
    append.append(f'f {ids[0]} {ids[2]} {ids[1]}\n')
    append.append(f'f {ids[3]} {ids[4]} {ids[5]}\n')
    append.append(f'f {ids[0]} {ids[1]} {ids[4]} {ids[3]}\n')
    append.append(f'f {ids[1]} {ids[2]} {ids[5]} {ids[4]}\n')
    append.append(f'f {ids[2]} {ids[0]} {ids[3]} {ids[5]}\n')

# -----------------------------------------------------------------
# V7 corrections
# -----------------------------------------------------------------
# 1) HEAVY, SOLID infill under the projecting central gable.
# The V6 triangle could visually disappear in some OBJ viewers; this one is thick.
cross_front=-58.8
cross_eave=31.8
cross_ridge=38.2
tri_prism_z('CentralFrontGable_SOLID',cross_front-0.85,cross_front+0.85,
            (-22,cross_eave),(0,cross_ridge),(22,cross_eave),'WhiteBrick')

# 2) Give the gable an actual architectural border instead of a raw triangle.
angle=math.atan2(cross_ridge-cross_eave,22.0)
sloped_len=math.hypot(22.0,cross_ridge-cross_eave)
# horizontal gable base
box('CentralGable_BaseTrim',0,cross_eave+0.05,cross_front-1.0,45.5,0.65,0.70,'ConcreteLight')
# slope trims follow the two roof lines
box('CentralGable_TrimL',-11,(cross_eave+cross_ridge)/2,cross_front-1.05,
    sloped_len,0.58,0.72,'ConcreteLight',rz=angle)
box('CentralGable_TrimR',11,(cross_eave+cross_ridge)/2,cross_front-1.05,
    sloped_len,0.58,0.72,'ConcreteLight',rz=-angle)

# 3) Gable vent/inset gives the upper facade detail and scale.
box('CentralGable_VentBacking',0,34.25,cross_front-1.10,8.8,2.7,0.22,'Green')
for i in range(5):
    box(f'CentralGable_VentSlat_{i}',0,33.35+i*0.43,cross_front-1.26,7.2,0.16,0.20,'MetalDark')

# 4) Add visible soffit and fascia under the projecting roof edge.
box('CentralGable_Soffit',0,31.35,-57.6,43.5,0.38,2.6,'Ceiling')
box('CentralGable_Fascia',0,31.55,-59.15,45.0,0.72,0.62,'RoofDark')

# 5) More depth around the balcony/central bay; no classroom door leaves added.
for x in (-20.5,20.5):
    box(f'CentralUpperPilaster_{x}',x,23.0,-47.85,0.75,10.0,1.35,'ConcreteLight')
box('CentralUpperBand',0,28.45,-47.9,42.0,0.55,0.55,'ConcreteLight')

# 6) Subtle roof-edge brackets, purely visual architectural detail.
for x in range(-18,19,6):
    box(f'GableBracket_{x}',x,30.75,-58.3,0.45,1.15,1.05,'RoofDark')

# Update model comment and append geometry after V6 body.
filtered.insert(1,'# V7: classroom openings only (no door leaves), reinforced central gable infill/details\n')
with open(os.path.join(out,'BlocoInstrucao_V7.obj'),'w',encoding='utf-8') as f:
    f.writelines(filtered)
    f.write('\n# --- V7 appended corrections ---\n')
    f.writelines(append)

# V7 uses same materials, with a renamed file.
with open(src_mtl,'r',encoding='utf-8') as f: mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V7.mtl'),'w',encoding='utf-8') as f: f.write(mtl)

readme='''BLOCO DE INSTRUCAO V7\n\nCORRECOES PRINCIPAIS:\n- Nenhuma folha de porta nas salas: somente vaos de entrada, deslocados para perto do canto.\n- Janelas continuam embutidas nas paredes externas de TODAS as salas e visiveis pelo interior.\n- Empena frontal central reforcada com volume solido e espesso para nao desaparecer em viewers OBJ.\n- Acabamento da empena com moldura inclinada, base, ventilacao e ripas.\n- Soffit/fascia sob a cobertura central para remover aspecto de telhado flutuando.\n- Mais pilastras, faixa superior e suportes na composicao central.\n\nOBJ mantem objetos nomeados para futura conversao seletiva em Parts/MeshParts.\n'''
with open(os.path.join(out,'README_V7.txt'),'w',encoding='utf-8') as f: f.write(readme)
print('V7 generated')
