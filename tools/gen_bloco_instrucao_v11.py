import os, runpy, math

# V11: rebuild BOTH complete side facades from scratch so the windows are real openings,
# not frames/glass sitting in front of any leftover wall. Also rebuild the side gables.
runpy.run_path('tools/gen_bloco_instrucao_v10.py', run_name='__main__')

src='models/v10/BlocoInstrucao_V10.obj'
src_mtl='models/v10/BlocoInstrucao_V10.mtl'
out='models/v11'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

# Remove EVERY old object belonging to the left/right exterior side facades,
# including corridor end walls/windows and V10 side window assemblies.
# Also remove the old side gable triangles so nothing can cover the new windows.
filtered=[]
skip=False
for line in lines:
    if line.startswith('o '):
        name=line[2:].strip()
        skip=(name.startswith('Side_L_') or name.startswith('Side_R_') or
              name.startswith('GableLeft') or name.startswith('GableRight'))
    if not skip:
        filtered.append(line)

for i,line in enumerate(filtered):
    if line.startswith('mtllib '):
        filtered[i]='mtllib BlocoInstrucao_V11.mtl\n'
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

def tri_prism_x(name,x1,x2,p1,p2,p3,mat='WhiteBrick'):
    use(name,mat)
    ids=[]
    for x in (x1,x2):
        for y,z in (p1,p2,p3):
            ids.append(addv((x,y,z)))
    append.append(f'f {ids[0]} {ids[1]} {ids[2]}\n')
    append.append(f'f {ids[5]} {ids[4]} {ids[3]}\n')
    append.append(f'f {ids[0]} {ids[3]} {ids[4]} {ids[1]}\n')
    append.append(f'f {ids[1]} {ids[4]} {ids[5]} {ids[2]}\n')
    append.append(f'f {ids[2]} {ids[5]} {ids[3]} {ids[0]}\n')

def wall_with_windows(prefix,x,y0,za,zb,side,window_centers,ww=8.6,wh=5.6,sill=4.0):
    """Build an X-normal wall with actual holes for the supplied windows."""
    H=14.0
    width=zb-za
    zc=(za+zb)/2

    # Horizontal bands under and over ALL windows. These do not cross the openings.
    box(prefix+'_Bottom',x,y0+sill/2,zc,1.0,sill,width,'WhiteBrick')
    top=H-sill-wh
    box(prefix+'_Top',x,y0+sill+wh+top/2,zc,1.0,top,width,'WhiteBrick')

    # Vertical wall piers between window openings.
    intervals=[]
    cursor=za
    for wz in sorted(window_centers):
        a=wz-ww/2
        b=wz+ww/2
        if a>cursor:
            intervals.append((cursor,a))
        cursor=b
    if cursor<zb:
        intervals.append((cursor,zb))
    for i,(a,b) in enumerate(intervals):
        if b-a>0.05:
            box(prefix+f'_Pier{i}',x,y0+sill+wh/2,(a+b)/2,1.0,wh,b-a,'WhiteBrick')

    outward=-1 if side=='L' else 1
    for i,wz in enumerate(window_centers,1):
        # Glass is centered THROUGH wall thickness: visible from outside and inside.
        box(prefix+f'_Window{i}_Glass',x,y0+sill+wh/2,wz,0.18,wh-0.48,ww-0.50,'Glass')
        # Frames are geometry, but they do not fill the opening.
        box(prefix+f'_Window{i}_FrameZ1',x+outward*0.12,y0+sill+wh/2,wz-ww/2,0.32,wh+0.55,0.26,'MetalDark')
        box(prefix+f'_Window{i}_FrameZ2',x+outward*0.12,y0+sill+wh/2,wz+ww/2,0.32,wh+0.55,0.26,'MetalDark')
        box(prefix+f'_Window{i}_FrameTop',x+outward*0.12,y0+sill+wh,wz,0.32,0.28,ww+0.55,'MetalDark')
        box(prefix+f'_Window{i}_FrameBottom',x+outward*0.12,y0+sill,wz,0.32,0.28,ww+0.55,'MetalDark')
        box(prefix+f'_Window{i}_Mullion',x,y0+sill+wh/2,wz,0.28,wh-0.25,0.16,'MetalDark')
        # Exterior sill/shade project outward only.
        box(prefix+f'_Window{i}_Sill',x+outward*0.48,y0+sill-0.18,wz,0.86,0.24,ww+0.90,'ConcreteLight')
        box(prefix+f'_Window{i}_Shade',x+outward*0.72,y0+sill+wh+0.45,wz,1.35,0.28,ww+1.20,'ConcreteLight')

# Complete side facade dimensions.
XLEFT=-92.0
XRIGHT=92.0
# front classroom wing / corridor end / back classroom wing
zones=[(-47.0,-10.0,'Front'),(-10.0,10.0,'Corridor'),(10.0,47.0,'Back')]

for floor in (0,1):
    y0=floor*14.0
    for side,x in [('L',XLEFT),('R',XRIGHT)]:
        # TWO real windows in the front classroom-end wall.
        wall_with_windows(f'V11_{side}_A{floor+1}_Front',x,y0,-47,-10,side,[-37.8,-19.2],ww=8.4)
        # ONE broad window at corridor end, centered.
        wall_with_windows(f'V11_{side}_A{floor+1}_Corridor',x,y0,-10,10,side,[0.0],ww=11.5,wh=6.0,sill=3.5)
        # TWO real windows in the back classroom-end wall.
        wall_with_windows(f'V11_{side}_A{floor+1}_Back',x,y0,10,47,side,[19.2,37.8],ww=8.4)

# Rebuild CLOSED side gables above the second floor. No giant blank floating face.
# Small vent keeps it visually alive but is above classroom height.
tri_prism_x('V11_GableLeft',-92.65,-91.85,(28,-47),(35.4,0),(28,47),'WhiteBrick')
tri_prism_x('V11_GableRight',91.85,92.65,(28,-47),(28,47),(35.4,0),'WhiteBrick')

# Gable trim and high vent on both sides.
for side,x,outward in [('L',-92.78,-1),('R',92.78,1)]:
    # high vent, centered in gable, not overlapping classroom windows
    box(f'V11_{side}_GableVentGlass',x,31.2,0,0.18,2.5,8.2,'Glass')
    box(f'V11_{side}_GableVentTop',x+outward*0.10,32.55,0,0.32,0.22,8.7,'MetalDark')
    box(f'V11_{side}_GableVentBottom',x+outward*0.10,29.85,0,0.32,0.22,8.7,'MetalDark')
    box(f'V11_{side}_GableVentFront',x+outward*0.10,31.2,-4.1,0.32,2.9,0.22,'MetalDark')
    box(f'V11_{side}_GableVentBack',x+outward*0.10,31.2,4.1,0.32,2.9,0.22,'MetalDark')

filtered.insert(1,'# V11: entire left/right facades rebuilt; side windows are genuine wall openings\n')
with open(os.path.join(out,'BlocoInstrucao_V11.obj'),'w',encoding='utf-8') as f:
    f.writelines(filtered)
    f.write('\n# --- V11 complete side facades ---\n')
    f.writelines(append)

with open(src_mtl,'r',encoding='utf-8') as f:
    mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V11.mtl'),'w',encoding='utf-8') as f:
    f.write(mtl)

readme='''BLOCO DE INSTRUCAO V11\n\nCORRECAO PRINCIPAL:\n- As fachadas laterais esquerda e direita foram removidas e reconstruidas INTEIRAS.\n- Nao existe mais parede solida escondida atras das janelas laterais.\n- Cada ponta de ala de salas tem 2 aberturas reais de janela por andar.\n- O corredor tem uma janela lateral ampla por andar.\n- Vidro atravessa a espessura da parede e e visivel por dentro e por fora.\n- Empenas laterais foram reconstruidas e fechadas, com pequeno respiro alto.\n- Nenhuma imagem foi gerada; este pacote contem OBJ + MTL.\n'''
with open(os.path.join(out,'README_V11.txt'),'w',encoding='utf-8') as f:
    f.write(readme)

print('V11 generated: complete side facades rebuilt with true openings')
