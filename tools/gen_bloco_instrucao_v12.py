import os, runpy, math

# V12: preserve V11 and rebuild ONLY the stair system + second-floor slab around it.
# Main fixes:
# - remove full second-floor slab that was blocking the staircase
# - create a real stairwell opening
# - rebuild stairs as SOLID stacked blocks (Roblox-like steps, not thin floating slabs)
# - proper U-turn landing and top landing
# - guardrails around the stair opening on the second floor
runpy.run_path('tools/gen_bloco_instrucao_v11.py', run_name='__main__')

src='models/v11/BlocoInstrucao_V11.obj'
src_mtl='models/v11/BlocoInstrucao_V11.mtl'
out='models/v12'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

# Remove old full second floor + old staircase/rail objects inherited from V6.
filtered=[]
skip=False
for line in lines:
    if line.startswith('o '):
        name=line[2:].strip()
        skip=(
            name == 'SecondFloor' or
            name.startswith('StairA') or
            name.startswith('StairB') or
            name.startswith('StairLanding') or
            name.startswith('StairBottomLanding') or
            name.startswith('StairTopLanding') or
            name.startswith('Rail_') or
            name.startswith('LandingRail')
        )
    if not skip:
        filtered.append(line)

for i,line in enumerate(filtered):
    if line.startswith('mtllib '):
        filtered[i]='mtllib BlocoInstrucao_V12.mtl\n'
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

def box(name,cx,cy,cz,sx,sy,sz,mat='Concrete',rx=0,ry=0,rz=0):
    use(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),
         (-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
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

# ------------------------------------------------------------------
# SECOND FLOOR WITH A REAL STAIRWELL OPENING
# ------------------------------------------------------------------
# Building bounds: X -92..92, Z -47..47.
# Stair opening: X -11..11, Z -34..-9.5.
# Rebuild slab in four pieces around the hole.
SLAB_Y=13.75
SLAB_T=0.50
hole_x1,hole_x2=-11.0,11.0
hole_z1,hole_z2=-34.0,-9.5

# Full-depth left/right slabs
box('V12_SecondFloor_Left',(-92+hole_x1)/2,SLAB_Y,0,hole_x1-(-92),SLAB_T,94,'Concrete')
box('V12_SecondFloor_Right',(hole_x2+92)/2,SLAB_Y,0,92-hole_x2,SLAB_T,94,'Concrete')
# Center strip before/after opening
box('V12_SecondFloor_Front',0,SLAB_Y,(-47+hole_z1)/2,hole_x2-hole_x1,SLAB_T,hole_z1-(-47),'Concrete')
box('V12_SecondFloor_Back',0,SLAB_Y,(hole_z2+47)/2,hole_x2-hole_x1,SLAB_T,47-hole_z2,'Concrete')

# Opening trim/fascia so the hole looks intentional, not broken.
trim_y=13.45
box('V12_OpeningTrim_Left',hole_x1,trim_y,(hole_z1+hole_z2)/2,0.38,0.75,hole_z2-hole_z1,'ConcreteLight')
box('V12_OpeningTrim_Right',hole_x2,trim_y,(hole_z1+hole_z2)/2,0.38,0.75,hole_z2-hole_z1,'ConcreteLight')
box('V12_OpeningTrim_Front',0,trim_y,hole_z1,hole_x2-hole_x1,0.75,0.38,'ConcreteLight')
box('V12_OpeningTrim_Back',0,trim_y,hole_z2,hole_x2-hole_x1,0.75,0.38,'ConcreteLight')

# ------------------------------------------------------------------
# SOLID U-SHAPED STAIRCASE
# ------------------------------------------------------------------
# Two parallel flights with a 180-degree turn on a broad landing.
# Each tread is a stacked solid block from its flight base to tread height.
STEP_RISE=0.50
STEP_RUN=1.18
STEPS=14
FLIGHT_W=7.6

# Flight A: starts near corridor side and runs toward front (negative Z).
A_X=-5.0
A_Z0=-12.2
for i in range(STEPS):
    h=(i+1)*STEP_RISE
    z=A_Z0-(i+0.5)*STEP_RUN
    box(f'V12_StairA_SolidStep_{i+1}',A_X,h/2,z,FLIGHT_W,h,STEP_RUN,'Concrete')

# Large turn landing at 7 studs high.
LAND_Y=7.0
LAND_Z=-30.1
box('V12_StairTurnLanding',0,LAND_Y-0.28,LAND_Z,18.8,0.56,7.6,'Concrete')

# Small connector blocks so neither flight visually floats off the landing.
box('V12_StairLandingConnector_A',-5,LAND_Y-0.25,-27.0,7.8,0.50,2.8,'Concrete')
box('V12_StairLandingConnector_B',5,LAND_Y-0.25,-27.0,7.8,0.50,2.8,'Concrete')

# Flight B: comes back toward corridor / upper hall.
B_X=5.0
B_Z0=-29.2
for i in range(STEPS):
    h=(i+1)*STEP_RISE
    z=B_Z0+(i+0.5)*STEP_RUN
    # solid from landing level to the tread height
    box(f'V12_StairB_SolidStep_{i+1}',B_X,LAND_Y+h/2,z,FLIGHT_W,h,STEP_RUN,'Concrete')

# Bottom and top landings aligned with actual floors.
box('V12_StairBottomLanding',A_X,0.16,-11.0,8.4,0.32,4.2,'Concrete')
box('V12_StairTopLanding',B_X,14.05,-11.0,8.4,0.36,4.2,'Concrete')

# ------------------------------------------------------------------
# HANDRAILS / GUARDS
# ------------------------------------------------------------------
# Posts along outside edges of each flight.
for i in range(8):
    # flight A
    z=A_Z0-(1.0+i*2.05)
    y=1.8+i*0.87
    box(f'V12_RailA_LeftPost_{i}',-9.0,y,z,0.18,3.3,0.18,'MetalDark')
    box(f'V12_RailA_RightPost_{i}',-1.0,y,z,0.18,3.3,0.18,'MetalDark')
    # flight B
    z=B_Z0+(1.0+i*2.05)
    y=LAND_Y+1.8+i*0.87
    box(f'V12_RailB_LeftPost_{i}',1.0,y,z,0.18,3.3,0.18,'MetalDark')
    box(f'V12_RailB_RightPost_{i}',9.0,y,z,0.18,3.3,0.18,'MetalDark')

# Landing guardrail at the turnaround.
for x in (-9,-6,-3,0,3,6,9):
    box(f'V12_LandingGuardPost_{x}',x,LAND_Y+1.75,-33.65,0.18,3.4,0.18,'MetalDark')
box('V12_LandingGuardTop',0,LAND_Y+3.35,-33.65,18.2,0.20,0.24,'MetalDark')
box('V12_LandingGuardMid',0,LAND_Y+1.85,-33.65,18.2,0.16,0.20,'MetalDark')

# Second-floor guardrail around the open stairwell, leaving the top landing side open.
# left and right long edges
for x in (hole_x1+0.35,hole_x2-0.35):
    for z in (-32,-28,-24,-20,-16):
        box(f'V12_UpperGuardPost_{x}_{z}',x,15.75,z,0.18,3.4,0.18,'MetalDark')
    box(f'V12_UpperGuardTop_{x}',x,17.35,(hole_z1+hole_z2)/2,0.22,0.22,hole_z2-hole_z1-1.0,'MetalDark')
    box(f'V12_UpperGuardMid_{x}',x,15.85,(hole_z1+hole_z2)/2,0.18,0.18,hole_z2-hole_z1-1.0,'MetalDark')
# front edge of opening
for x in (-8,-4,0,4,8):
    box(f'V12_UpperFrontGuardPost_{x}',x,15.75,hole_z1+0.35,0.18,3.4,0.18,'MetalDark')
box('V12_UpperFrontGuardTop',0,17.35,hole_z1+0.35,20.5,0.22,0.22,'MetalDark')
box('V12_UpperFrontGuardMid',0,15.85,hole_z1+0.35,20.5,0.18,0.18,'MetalDark')

# Simple wall-base protection around stair core.
box('V12_StairBaseSkirt_Left',-10.55,1.0,-21.8,0.14,2.0,23.0,'Green')
box('V12_StairBaseSkirt_Right',10.55,1.0,-21.8,0.14,2.0,23.0,'Green')

filtered.insert(1,'# V12: stairwell opening + solid stacked U-stair rebuilt; second-floor slab no longer blocks staircase\n')
with open(os.path.join(out,'BlocoInstrucao_V12.obj'),'w',encoding='utf-8') as f:
    f.writelines(filtered)
    f.write('\n# --- V12 stair system correction ---\n')
    f.writelines(append)

with open(src_mtl,'r',encoding='utf-8') as f:
    mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V12.mtl'),'w',encoding='utf-8') as f:
    f.write(mtl)

readme='''BLOCO DE INSTRUCAO V12\n\nCORRECAO PRINCIPAL: ESCADARIA\n- Removida a laje inteira que atravessava/bloqueava a escada.\n- Segundo piso reconstruido em partes, deixando um vao real de escadaria.\n- Escada em U refeita com degraus SOLIDOS empilhados, como uma escada feita por Parts no Roblox.\n- Patamar de retorno maior e conectado aos dois lances.\n- Patamar superior conecta corretamente ao segundo andar.\n- Guarda-corpos adicionados no patamar e ao redor do vao do segundo piso.\n- Fachadas, janelas, telhado e demais partes do V11 foram preservadas.\n- Sem geracao de imagem: pacote OBJ + MTL.\n'''
with open(os.path.join(out,'README_V12.txt'),'w',encoding='utf-8') as f:
    f.write(readme)

print('V12 generated: stairwell rebuilt correctly')
