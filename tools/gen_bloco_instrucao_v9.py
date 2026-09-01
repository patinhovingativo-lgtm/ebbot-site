import os, runpy, math

# Build V8 first, then close the remaining roof/pavilion gaps seen in review screenshots.
runpy.run_path('tools/gen_bloco_instrucao_v8.py', run_name='__main__')

src='models/v8/BlocoInstrucao_V8.obj'
src_mtl='models/v8/BlocoInstrucao_V8.mtl'
out='models/v9'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

for i,line in enumerate(lines):
    if line.startswith('mtllib '):
        lines[i]='mtllib BlocoInstrucao_V9.mtl\n'
        break

vertex_count=sum(1 for l in lines if l.startswith('v '))
append=[]

def addv(p):
    global vertex_count
    vertex_count += 1
    append.append('v %.5f %.5f %.5f\n' % p)
    return vertex_count

def use(name,mat):
    append.append(f'o {name}\n')
    append.append(f'usemtl {mat}\n')

def box(name,cx,cy,cz,sx,sy,sz,mat='WhiteBrick',rx=0,ry=0,rz=0):
    use(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),(-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    cr,sr=math.cos(rx),math.sin(rx)
    cyy,syy=math.cos(ry),math.sin(ry)
    czr,szr=math.cos(rz),math.sin(rz)
    ids=[]
    for x,y,z in pts:
        y,z=y*cr-z*sr,y*sr+z*cr
        x,z=x*cyy+z*syy,-x*syy+z*cyy
        x,y=x*czr-y*szr,x*szr+y*czr
        ids.append(addv((cx+x,cy+y,cz+z)))
    for a,b,c,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        append.append(f'f {ids[a]} {ids[b]} {ids[c]} {ids[d]}\n')

def tri_prism_z(name,z1,z2,p1,p2,p3,mat='WhiteBrick'):
    use(name,mat)
    ids=[]
    for z in (z1,z2):
        for x,y in (p1,p2,p3):
            ids.append(addv((x,y,z)))
    append.append(f'f {ids[0]} {ids[2]} {ids[1]}\n')
    append.append(f'f {ids[3]} {ids[4]} {ids[5]}\n')
    append.append(f'f {ids[0]} {ids[1]} {ids[4]} {ids[3]}\n')
    append.append(f'f {ids[1]} {ids[2]} {ids[5]} {ids[4]}\n')
    append.append(f'f {ids[2]} {ids[0]} {ids[3]} {ids[5]}\n')

def quad_prism(name,p1,p2,p3,p4,depth_axis='z',depth=0.7,mat='WhiteBrick'):
    use(name,mat)
    ids=[]
    if depth_axis=='z':
        for dz in (-depth/2,depth/2):
            for x,y,z in (p1,p2,p3,p4): ids.append(addv((x,y,z+dz)))
    else:
        for dx in (-depth/2,depth/2):
            for x,y,z in (p1,p2,p3,p4): ids.append(addv((x+dx,y,z)))
    # front/back quads and sides
    append.append(f'f {ids[0]} {ids[1]} {ids[2]} {ids[3]}\n')
    append.append(f'f {ids[7]} {ids[6]} {ids[5]} {ids[4]}\n')
    for a,b in [(0,1),(1,2),(2,3),(3,0)]:
        append.append(f'f {ids[a]} {ids[b]} {ids[b+4]} {ids[a+4]}\n')

# ------------------------------------------------------------
# V9 corrections
# ------------------------------------------------------------
# Existing roof geometry values from V6/V8.
main_eave_y=28.75
main_ridge_y=35.4
main_eave_z=50.2
cross_z1=-58.8
cross_z2=-19.0
cross_eave=31.8
cross_ridge=38.2
cross_half_w=22.0

# 1) Close the REAR triangular end of the projecting cross-gable.
# V6/V7 had a front gable; this seals the opposite triangular end too.
tri_prism_z('CrossGableRear_SOLID',cross_z2-0.75,cross_z2+0.75,
            (-cross_half_w,cross_eave),(0,cross_ridge),(cross_half_w,cross_eave),'WhiteBrick')

# 2) Give that rear gable a proper frame and a small high vent so it is not a dead blank wall.
angle=math.atan2(cross_ridge-cross_eave,cross_half_w)
sloped=math.hypot(cross_half_w,cross_ridge-cross_eave)
box('CrossRearGableBaseTrim',0,cross_eave+0.05,cross_z2+0.86,45.0,0.55,0.55,'ConcreteLight')
box('CrossRearGableTrimL',-11,(cross_eave+cross_ridge)/2,cross_z2+0.90,sloped,0.48,0.55,'ConcreteLight',rz=angle)
box('CrossRearGableTrimR',11,(cross_eave+cross_ridge)/2,cross_z2+0.90,sloped,0.48,0.55,'ConcreteLight',rz=-angle)
box('CrossRearVentBacking',0,34.2,cross_z2+0.95,9.2,2.6,0.18,'Green')
for i in range(5):
    box(f'CrossRearVentSlat_{i}',0,33.35+i*0.42,cross_z2+1.08,7.6,0.14,0.14,'MetalDark')

# 3) Seal the visible roof-valley gap where the cross-gable meets the main roof.
# The main roof front slope at a given z<0 is linear from ridge at z=0 to eave at -50.2.
def main_roof_y(z):
    t=min(max(abs(z)/main_eave_z,0.0),1.0)
    return main_ridge_y-(main_ridge_y-main_eave_y)*t

# Create vertical apron/infill walls along both sides of the cross pavilion.
# They start near z=-47 (main facade) and run back to z=-19, following the main roof underneath.
for side,x in [('L',-cross_half_w),('R',cross_half_w)]:
    # break into strips so lower edge follows the main roof slope smoothly
    z_start=-47.0
    z_end=cross_z2
    segments=14
    for i in range(segments):
        za=z_start+(z_end-z_start)*i/segments
        zb=z_start+(z_end-z_start)*(i+1)/segments
        zc=(za+zb)/2
        ya=main_roof_y(za)+0.08
        yb=main_roof_y(zb)+0.08
        # Cross roof side eave stays around 31.8 along x=+-22.
        top=cross_eave-0.15
        bottom=(ya+yb)/2
        h=max(0.2,top-bottom)
        box(f'CrossRoofSideInfill_{side}_{i}',x, bottom+h/2, zc,0.62,h,abs(zb-za)+0.18,'WhiteBrick')

# 4) Add dark flashing/valley caps over the roof intersection to hide seams.
for side,x in [('L',-cross_half_w-0.35),('R',cross_half_w+0.35)]:
    z_start=-47.0
    z_end=cross_z2
    segments=14
    for i in range(segments):
        za=z_start+(z_end-z_start)*i/segments
        zb=z_start+(z_end-z_start)*(i+1)/segments
        zc=(za+zb)/2
        y=main_roof_y(zc)+0.24
        box(f'ValleyFlashing_{side}_{i}',x,y,zc,0.72,0.18,abs(zb-za)+0.25,'RoofDark')

# 5) Fill the broad blank upper side faces with actual architectural window bands.
# These are high stair/hall windows, not classroom windows and do not create new doors.
for side,x in [('L',-15.50),('R',15.50)]:
    for zc in (-41.5,-34.5,-27.5):
        box(f'UpperHallWindow_{side}_{zc}_Glass',x,22.0,zc,0.18,4.6,5.0,'Glass')
        box(f'UpperHallWindow_{side}_{zc}_Top',x,24.45,zc,0.32,0.22,5.5,'MetalDark')
        box(f'UpperHallWindow_{side}_{zc}_Bottom',x,19.55,zc,0.32,0.22,5.5,'MetalDark')
        box(f'UpperHallWindow_{side}_{zc}_Front',x,22.0,zc-2.5,0.32,5.1,0.22,'MetalDark')
        box(f'UpperHallWindow_{side}_{zc}_Back',x,22.0,zc+2.5,0.32,5.1,0.22,'MetalDark')

# 6) Add a continuous soffit strip beneath the cross-roof side eaves to eliminate floating-roof appearance.
for side,x in [('L',-21.65),('R',21.65)]:
    box(f'CrossSideSoffit_{side}',x,31.42,(cross_z1+cross_z2)/2,0.55,0.35,(cross_z2-cross_z1)+1.4,'Ceiling')

lines.insert(1,'# V9: rear cross-gable sealed, roof-valley gaps filled, side faces detailed\n')
with open(os.path.join(out,'BlocoInstrucao_V9.obj'),'w',encoding='utf-8') as f:
    f.writelines(lines)
    f.write('\n# --- V9 roof/gable infill corrections ---\n')
    f.writelines(append)

with open(src_mtl,'r',encoding='utf-8') as f:
    mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V9.mtl'),'w',encoding='utf-8') as f:
    f.write(mtl)

readme='''BLOCO DE INSTRUCAO V9\n\nCORRECOES DE FECHAMENTO:\n- Empena traseira do telhado central agora e totalmente fechada.\n- Lacunas/vales visiveis entre o telhado central e o telhado principal receberam preenchimento lateral.\n- Adicionado flashing/acabamento nas intersecoes do telhado.\n- Soffit lateral fecha o aspecto de cobertura flutuante.\n- Faces laterais altas ganharam faixas de janelas do hall/escada para quebrar paredes cegas.\n- Mantidas as janelas de observacao das salas voltadas para o corredor e os vaos de entrada sem folhas de porta.\n'''
with open(os.path.join(out,'README_V9.txt'),'w',encoding='utf-8') as f:
    f.write(readme)
print('V9 generated')
