import os, runpy, math

# V13: preserve V12 building, but rebuild ONLY the staircase/stairwell.
# Goal: a proper U-shaped staircase with a CURVED semicircular turnaround,
# open underside, clean connection to both floors, and continuous rails.
runpy.run_path('tools/gen_bloco_instrucao_v12.py', run_name='__main__')

src='models/v12/BlocoInstrucao_V12.obj'
src_mtl='models/v12/BlocoInstrucao_V12.mtl'
out='models/v13'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

# Remove every V12 stair/stairwell object so nothing overlaps the new geometry.
filtered=[]
skip=False
remove_prefixes=(
    'V12_SecondFloor_',
    'V12_OpeningTrim_',
    'V12_Stair',
    'V12_Rail',
    'V12_Landing',
    'V12_UpperGuard',
)
remove_exact={
    'V12_StairBaseSkirt_Left',
    'V12_StairBaseSkirt_Right',
}
for line in lines:
    if line.startswith('o '):
        name=line[2:].strip()
        skip=(name in remove_exact or any(name.startswith(p) for p in remove_prefixes))
    if not skip:
        filtered.append(line)

for i,line in enumerate(filtered):
    if line.startswith('mtllib '):
        filtered[i]='mtllib BlocoInstrucao_V13.mtl\n'
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

def half_disk_prism(name,cx,cy,cz,radius,thickness,mat='Concrete',segments=24):
    """Half disk opening toward +Z; curved edge extends toward -Z."""
    use(name,mat)
    yb=cy-thickness/2
    yt=cy+thickness/2
    # perimeter from x=-R to +R around the negative-Z semicircle
    perimeter=[]
    for i in range(segments+1):
        a=math.pi + math.pi*i/segments
        perimeter.append((cx+radius*math.cos(a), cz+radius*math.sin(a)))
    # top/bottom centers
    top_center=addv((cx,yt,cz))
    bot_center=addv((cx,yb,cz))
    top_ids=[addv((x,yt,z)) for x,z in perimeter]
    bot_ids=[addv((x,yb,z)) for x,z in perimeter]
    # top / bottom fans
    for i in range(len(perimeter)-1):
        append.append(f'f {top_center} {top_ids[i]} {top_ids[i+1]}\n')
        append.append(f'f {bot_center} {bot_ids[i+1]} {bot_ids[i]}\n')
    # curved side
    for i in range(len(perimeter)-1):
        append.append(f'f {bot_ids[i]} {bot_ids[i+1]} {top_ids[i+1]} {top_ids[i]}\n')
    # diameter side (end points back to each other)
    append.append(f'f {bot_ids[-1]} {bot_ids[0]} {top_ids[0]} {top_ids[-1]}\n')

# ------------------------------------------------------------------
# REAL STAIRWELL OPENING IN SECOND FLOOR
# ------------------------------------------------------------------
SLAB_Y=13.75
SLAB_T=0.50
hole_x1,hole_x2=-11.7,11.7
hole_z1,hole_z2=-37.2,-8.8

box('V13_SecondFloor_Left',(-92+hole_x1)/2,SLAB_Y,0,hole_x1+92,SLAB_T,94,'Concrete')
box('V13_SecondFloor_Right',(hole_x2+92)/2,SLAB_Y,0,92-hole_x2,SLAB_T,94,'Concrete')
box('V13_SecondFloor_Front',0,SLAB_Y,(-47+hole_z1)/2,hole_x2-hole_x1,SLAB_T,hole_z1+47,'Concrete')
box('V13_SecondFloor_Back',0,SLAB_Y,(hole_z2+47)/2,hole_x2-hole_x1,SLAB_T,47-hole_z2,'Concrete')

# clean fascia around the opening
trim_y=13.43
box('V13_OpeningTrim_Left',hole_x1,trim_y,(hole_z1+hole_z2)/2,0.40,0.82,hole_z2-hole_z1,'ConcreteLight')
box('V13_OpeningTrim_Right',hole_x2,trim_y,(hole_z1+hole_z2)/2,0.40,0.82,hole_z2-hole_z1,'ConcreteLight')
box('V13_OpeningTrim_Front',0,trim_y,hole_z1,hole_x2-hole_x1,0.82,0.40,'ConcreteLight')
box('V13_OpeningTrim_Back',0,trim_y,hole_z2,hole_x2-hole_x1,0.82,0.40,'ConcreteLight')

# ------------------------------------------------------------------
# CURVED U STAIR
# ------------------------------------------------------------------
# Two parallel flights. At half height they meet a semicircular landing,
# giving the stair an actual architectural curve instead of a square U.
STEP_RISE=0.50
STEP_RUN=1.02
STEPS=14
FLIGHT_W=7.8
TREAD_T=0.50
A_X=-5.15
B_X=5.15
START_Z=-10.9
LAND_EDGE_Z=-25.2
LAND_Y=7.0
LAND_R=10.15
LAND_CZ=LAND_EDGE_Z

# Lower bottom landing
box('V13_BottomLanding',A_X,0.16,-9.8,8.7,0.32,4.0,'Concrete')

# Flight A: ground -> curved landing, moving toward -Z.
for i in range(STEPS):
    top=(i+1)*STEP_RISE
    z=START_Z-(i+0.5)*STEP_RUN
    box(f'V13_StairA_Tread_{i+1}',A_X,top-TREAD_T/2,z,FLIGHT_W,TREAD_T,STEP_RUN+0.05,'Concrete')

# Curved mid-level turnaround. Diameter is aligned with ends of both flights.
half_disk_prism('V13_CurvedTurnLanding',0,LAND_Y-0.30,LAND_CZ,LAND_R,0.60,'Concrete',segments=28)
# Slight straight bridge along the diameter so both flights meet it cleanly.
box('V13_CurvedLandingBridge',0,LAND_Y-0.30,LAND_EDGE_Z+0.62,20.0,0.60,1.25,'Concrete')

# Flight B: curved landing -> second floor, moving back toward +Z.
# First tread starts immediately after the curved landing diameter.
B_START_Z=LAND_EDGE_Z+0.40
for i in range(STEPS):
    top=LAND_Y+(i+1)*STEP_RISE
    z=B_START_Z+(i+0.5)*STEP_RUN
    box(f'V13_StairB_Tread_{i+1}',B_X,top-TREAD_T/2,z,FLIGHT_W,TREAD_T,STEP_RUN+0.05,'Concrete')

# Top landing aligned with second-floor slab.
box('V13_TopLanding',B_X,14.03,-9.55,8.7,0.38,4.3,'Concrete')

# Under-flight side stringers make the staircase look supported without becoming a giant solid wall.
def stringer(name,x,z1,y1,z2,y2):
    dz=z2-z1
    dy=y2-y1
    length=math.hypot(dz,dy)
    angle=-math.atan2(dy,dz)
    box(name,x,(y1+y2)/2,(z1+z2)/2,0.34,0.62,length,'ConcreteLight',rx=angle)

# outer + inner stringers for each flight
for x in (A_X-FLIGHT_W/2+0.25,A_X+FLIGHT_W/2-0.25):
    stringer(f'V13_StringerA_{x}',x,START_Z,0.15,LAND_EDGE_Z,LAND_Y-0.15)
for x in (B_X-FLIGHT_W/2+0.25,B_X+FLIGHT_W/2-0.25):
    stringer(f'V13_StringerB_{x}',x,LAND_EDGE_Z,LAND_Y+0.15,-10.7,13.85)

# ------------------------------------------------------------------
# RAILINGS: continuous sloped rails + curved landing rail
# ------------------------------------------------------------------
RAIL_H=3.15

def rail_line(name,x,z1,y1,z2,y2):
    dz=z2-z1
    dy=y2-y1
    length=math.hypot(dz,dy)
    angle=-math.atan2(dy,dz)
    box(name,x,(y1+y2)/2,(z1+z2)/2,0.20,0.20,length,'MetalDark',rx=angle)

# posts on both sides, but less dense/cleaner
for idx in range(0,STEPS+1,2):
    y=min(LAND_Y,idx*STEP_RISE)+RAIL_H/2
    z=START_Z-idx*STEP_RUN
    for x in (A_X-FLIGHT_W/2-0.18,A_X+FLIGHT_W/2+0.18):
        box(f'V13_RailA_Post_{idx}_{x}',x,y,z,0.18,RAIL_H,0.18,'MetalDark')

for idx in range(0,STEPS+1,2):
    base=LAND_Y+idx*STEP_RISE
    y=base+RAIL_H/2
    z=B_START_Z+idx*STEP_RUN
    for x in (B_X-FLIGHT_W/2-0.18,B_X+FLIGHT_W/2+0.18):
        box(f'V13_RailB_Post_{idx}_{x}',x,y,z,0.18,RAIL_H,0.18,'MetalDark')

# sloped top rails
for x in (A_X-FLIGHT_W/2-0.18,A_X+FLIGHT_W/2+0.18):
    rail_line(f'V13_RailA_Top_{x}',x,START_Z,RAIL_H,LAND_EDGE_Z,LAND_Y+RAIL_H)
for x in (B_X-FLIGHT_W/2-0.18,B_X+FLIGHT_W/2+0.18):
    rail_line(f'V13_RailB_Top_{x}',x,LAND_EDGE_Z,LAND_Y+RAIL_H,-10.7,14+RAIL_H)

# curved guard rail around the semicircular edge
CURVE_R=LAND_R+0.10
curve_segments=18
prev=None
for i in range(curve_segments+1):
    a=math.pi + math.pi*i/curve_segments
    x=CURVE_R*math.cos(a)
    z=LAND_CZ+CURVE_R*math.sin(a)
    box(f'V13_CurveRailPost_{i}',x,LAND_Y+RAIL_H/2,z,0.18,RAIL_H,0.18,'MetalDark')
    if prev is not None:
        px,pz=prev
        dx=x-px; dz=z-pz
        seg=math.hypot(dx,dz)
        yaw=math.atan2(dx,dz)
        box(f'V13_CurveRailTop_{i}',(x+px)/2,LAND_Y+RAIL_H,(z+pz)/2,0.20,0.20,seg,'MetalDark',ry=yaw)
    prev=(x,z)

# ------------------------------------------------------------------
# SECOND FLOOR GUARD AROUND OPENING
# ------------------------------------------------------------------
# Keep the back/top-landing side open only where the player exits the stairs.
GUARD_Y=15.72
# long outer sides
for x in (hole_x1+0.32,hole_x2-0.32):
    for z in (-35,-31,-27,-23,-19,-15):
        box(f'V13_UpperGuardPost_{x}_{z}',x,GUARD_Y,z,0.18,3.30,0.18,'MetalDark')
    box(f'V13_UpperGuardTop_{x}',x,17.30,(hole_z1-10.4)/2,0.20,0.20,(-10.4)-hole_z1,'MetalDark')
# front edge around curved landing
for x in (-9,-6,-3,0,3,6,9):
    box(f'V13_UpperFrontPost_{x}',x,GUARD_Y,hole_z1+0.38,0.18,3.30,0.18,'MetalDark')
box('V13_UpperFrontTop',0,17.30,hole_z1+0.38,21.8,0.20,0.20,'MetalDark')
# short back guards left/right of top landing exit
box('V13_UpperBackGuard_L',-8.6,17.30,hole_z2+0.38,6.0,0.20,0.20,'MetalDark')
box('V13_UpperBackGuard_R',8.6,17.30,hole_z2+0.38,6.0,0.20,0.20,'MetalDark')
for x in (-11.0,-6.2,6.2,11.0):
    box(f'V13_UpperBackPost_{x}',x,GUARD_Y,hole_z2+0.38,0.18,3.30,0.18,'MetalDark')

# low wall-base finish around stair core
box('V13_StairSkirt_Left',-11.15,1.0,-22.6,0.14,2.0,26.5,'Green')
box('V13_StairSkirt_Right',11.15,1.0,-22.6,0.14,2.0,26.5,'Green')

filtered.insert(1,'# V13: staircase rebuilt as a clean U stair with semicircular curved turnaround and open underside\n')
with open(os.path.join(out,'BlocoInstrucao_V13.obj'),'w',encoding='utf-8') as f:
    f.writelines(filtered)
    f.write('\n# --- V13 curved staircase ---\n')
    f.writelines(append)

with open(src_mtl,'r',encoding='utf-8') as f:
    mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V13.mtl'),'w',encoding='utf-8') as f:
    f.write(mtl)

readme='''BLOCO DE INSTRUCAO V13\n\nESCADARIA REFEITA:\n- Escada em U de dois lances, agora com retorno SEMICIRCULAR curvo.\n- Nada de escada reta atravessando o ambiente.\n- Vao do segundo piso recalculado para a nova escada.\n- Degraus separados e limpos, com parte inferior aberta e longarinas de suporte.\n- Patamar curvo conecta os dois lances a 7 studs de altura.\n- Patamar superior conecta diretamente ao segundo andar.\n- Corrimaos inclinados acompanham os lances.\n- Guarda-corpo curvo acompanha o patamar semicircular.\n- Guarda-corpo protege o vao no segundo andar sem bloquear a saida da escada.\n- Fachadas, janelas, telhado, varanda e salas foram preservados.\n- Arquivo OBJ + MTL; nenhuma imagem gerada.\n'''
with open(os.path.join(out,'README_V13.txt'),'w',encoding='utf-8') as f:
    f.write(readme)

print('V13 generated: curved U staircase rebuilt')
