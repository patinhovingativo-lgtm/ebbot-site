import math, os

OUT='models/v5'
os.makedirs(OUT, exist_ok=True)
obj=[]; verts=[]; faces=[]; mats=[]; names=[]

def addv(v):
    verts.append(tuple(v)); return len(verts)

def use(name, mat):
    obj.append(f'o {name}'); obj.append(f'usemtl {mat}')

def box(name, cx,cy,cz, sx,sy,sz, mat='WhiteBrick', rx=0, ry=0, rz=0):
    use(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),(-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    cr,sr=math.cos(rx),math.sin(rx); cyy,syy=math.cos(ry),math.sin(ry); czr,szr=math.cos(rz),math.sin(rz)
    out=[]
    for x,y,z in pts:
        # X
        y,z=y*cr-z*sr, y*sr+z*cr
        # Y
        x,z=x*cyy+z*syy, -x*syy+z*cyy
        # Z
        x,y=x*czr-y*szr, x*szr+y*czr
        out.append(addv((cx+x,cy+y,cz+z)))
    q=[(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]
    for a,b,c,d in q: obj.append(f'f {out[a]} {out[b]} {out[c]} {out[d]}')

def quad(name, p1,p2,p3,p4, mat):
    use(name,mat); ids=[addv(p) for p in (p1,p2,p3,p4)]; obj.append('f '+' '.join(map(str,ids)))

def frame_window(prefix, cx, y0, z, face='front', w=8.4, h=5.4, sill=4.0):
    # glass and physical-looking frame pieces; wall opening is created separately
    depth=0.22
    sign=-1 if face=='front' else 1
    zf=z-sign*0.22
    box(prefix+'_Glass',cx,y0+sill+h/2,zf,w-0.45,h-0.45,0.18,'Glass')
    t=0.28
    box(prefix+'_FL',cx-w/2,y0+sill+h/2,zf-sign*0.11,t,h+0.7,0.35,'MetalDark')
    box(prefix+'_FR',cx+w/2,y0+sill+h/2,zf-sign*0.11,t,h+0.7,0.35,'MetalDark')
    box(prefix+'_FT',cx,y0+sill+h,zf-sign*0.11,w+0.6,t,0.35,'MetalDark')
    box(prefix+'_FB',cx,y0+sill,zf-sign*0.11,w+0.6,t,0.35,'MetalDark')
    box(prefix+'_MidV',cx,y0+sill+h/2,zf-sign*0.14,0.16,h-0.3,0.38,'MetalDark')
    box(prefix+'_Sill',cx,y0+sill-0.18,zf-sign*0.35,w+1.0,0.25,0.9,'ConcreteLight')
    # modest sunshade above window
    box(prefix+'_Shade',cx,y0+sill+h+0.5,zf-sign*0.62,w+1.3,0.28,1.25,'ConcreteLight')

def exterior_room_wall(prefix, xa, xb, y0, z, face):
    # two spaced windows in an actual wall opening
    w=8.4; h=5.4; sill=4.0
    width=xb-xa; cx=(xa+xb)/2
    offs=width*0.23
    wx=[cx-offs,cx+offs]
    # bottom and top bands
    box(prefix+'_Bottom',cx,y0+sill/2,z,width,sill,1,'WhiteBrick')
    top=14-sill-h
    box(prefix+'_Top',cx,y0+sill+h+top/2,z,width,top,1,'WhiteBrick')
    seg=[(xa,wx[0]-w/2),(wx[0]+w/2,wx[1]-w/2),(wx[1]+w/2,xb)]
    for i,(a,b) in enumerate(seg):
        if b>a: box(prefix+f'_Mid{i}',(a+b)/2,y0+sill+h/2,z,b-a,h,1,'WhiteBrick')
    for i,x in enumerate(wx): frame_window(prefix+f'_W{i+1}',x,y0,z,face,w,h,sill)
    # green base accent on facade, visual projection
    sign=-1 if face=='front' else 1
    box(prefix+'_GreenBase',cx,y0+1.0,z-sign*0.54,width,2.0,0.08,'Green')

def door_wall(prefix, xa, xb, y0, z, doorx=None):
    dw,dh=6.0,9.0; cx=(xa+xb)/2 if doorx is None else doorx
    # segments around opening
    if cx-dw/2>xa: box(prefix+'_L',(xa+cx-dw/2)/2,y0+7,z,(cx-dw/2)-xa,14,1,'WhiteBrick')
    if xb>cx+dw/2: box(prefix+'_R',(cx+dw/2+xb)/2,y0+7,z,xb-(cx+dw/2),14,1,'WhiteBrick')
    box(prefix+'_Top',cx,y0+dh+(14-dh)/2,z,dw,14-dh,1,'WhiteBrick')
    # dark frame, no door leaf
    box(prefix+'_FrameL',cx-dw/2,y0+dh/2,z-0.22,0.25,dh+0.5,0.35,'MetalDark')
    box(prefix+'_FrameR',cx+dw/2,y0+dh/2,z-0.22,0.25,dh+0.5,0.35,'MetalDark')
    box(prefix+'_FrameT',cx,y0+dh,z-0.22,dw+0.5,0.25,0.35,'MetalDark')

def stair_flight(prefix, x, z0, zdir, y0, steps=14, width=7.5, run=1.15, rise=0.5):
    for i in range(steps):
        top=y0+(i+1)*rise
        z=z0+zdir*(i+0.5)*run
        box(prefix+f'_Step{i+1}',x,top-rise/2,z,width,rise,run,'Concrete')

def rail_posts(prefix, xs, zs, ybase, height=3.3):
    for i,(x,z) in enumerate(zip(xs,zs)):
        box(prefix+f'_Post{i}',x,ybase+height/2,z,0.16,height,0.16,'MetalDark')

# ---------------- dimensions ----------------
W=176.0; D=90.0; FH=14.0; H=28.0; COR=18.0
front_z=-45.0; back_z=45.0; cf=-9.0; cb=9.0
# front rooms: 4 rooms around a 28-wide central core
front_ranges=[(-88,-51),(-51,-14),(14,51),(51,88)]
back_ranges=[(-88,-44),(-44,0),(0,44),(44,88)]

# slabs, ceilings, foundation
box('Foundation',0,-0.45,0,W+6,0.9,D+6,'Concrete')
box('GroundFloor',0,-0.15,0,W,0.3,D,'Floor')
box('SecondFloor',0,13.75,0,W,0.5,D,'Concrete')
box('CeilingTop',0,27.75,0,W,0.5,D,'ConcreteLight')
# exterior side walls
box('OuterLeft',-88,14,0,1,28,D,'WhiteBrick'); box('OuterRight',88,14,0,1,28,D,'WhiteBrick')
# side green accents and shallow vertical pilasters
box('LeftGreenBase',-88.54,1,0,0.08,2,D,'Green'); box('RightGreenBase',88.54,1,0,0.08,2,D,'Green')
for z in (-30,0,30):
    box('LeftPil_'+str(z),-88.7,14,z,0.45,28,1.2,'ConcreteLight')
    box('RightPil_'+str(z),88.7,14,z,0.45,28,1.2,'ConcreteLight')

# rooms on both floors
for floor in (0,1):
    y0=floor*FH
    # front row
    for i,(xa,xb) in enumerate(front_ranges,1):
        exterior_room_wall(f'A{floor+1}_FrontRoom{i}',xa,xb,y0,front_z,'front')
        door_wall(f'A{floor+1}_FrontDoor{i}',xa,xb,y0,cf)
    # back row
    for i,(xa,xb) in enumerate(back_ranges,1):
        exterior_room_wall(f'A{floor+1}_BackRoom{i}',xa,xb,y0,back_z,'back')
        door_wall(f'A{floor+1}_BackDoor{i}',xa,xb,y0,cb)
    # front room dividers
    for x in (-51,-14,14,51):
        # avoid core boundary duplicating deep walls on x ±14 only where needed
        if x in (-51,51): box(f'A{floor+1}_FDiv{x}',x,y0+7,(front_z+cf)/2,1,14,front_z*-1-cf if False else 36,'WhiteBrick')
    # exact divider lengths
    box(f'A{floor+1}_FDiv_m51',-51,y0+7,-27,1,14,36,'WhiteBrick')
    box(f'A{floor+1}_FDiv_p51',51,y0+7,-27,1,14,36,'WhiteBrick')
    box(f'A{floor+1}_FCoreL',-14,y0+7,-27,1,14,36,'WhiteBrick')
    box(f'A{floor+1}_FCoreR',14,y0+7,-27,1,14,36,'WhiteBrick')
    # back room dividers
    for x in (-44,0,44): box(f'A{floor+1}_BDiv{x}',x,y0+7,27,1,14,36,'WhiteBrick')
    # corridor skirting + ceiling light strips
    box(f'A{floor+1}_CorridorSkirtF',0,y0+0.55,cf+0.54,W,1.1,0.08,'Green')
    box(f'A{floor+1}_CorridorSkirtB',0,y0+0.55,cb-0.54,W,1.1,0.08,'Green')
    for lx in range(-75,76,25):
        box(f'A{floor+1}_Light_{lx}',lx,y0+13.35,0,7.5,0.12,1.6,'LightPanel')

# central front stair/entrance core exterior at x ±14, z -45..-9 already bounded by walls above
# front facade central lower: entrance opening 12x10
for floor in (0,1):
    y0=floor*FH
    if floor==0:
        box('EntryFacadeL',-10,y0+7,front_z,8,14,1,'WhiteBrick')
        box('EntryFacadeR',10,y0+7,front_z,8,14,1,'WhiteBrick')
        box('EntryHeader',0,y0+12,front_z,12,4,1,'WhiteBrick')
        box('EntryFrameL',-6,y0+5,front_z-0.24,0.32,10.2,0.4,'MetalDark')
        box('EntryFrameR',6,y0+5,front_z-0.24,0.32,10.2,0.4,'MetalDark')
        box('EntryFrameT',0,y0+10,front_z-0.24,12.3,0.32,0.4,'MetalDark')
    else:
        # balcony door opening centered, plus flanking tall windows
        box('BalFacadeL',-10,y0+7,front_z,8,14,1,'WhiteBrick')
        box('BalFacadeR',10,y0+7,front_z,8,14,1,'WhiteBrick')
        box('BalHeader',0,y0+12,front_z,12,4,1,'WhiteBrick')
        box('BalDoorGlass',0,y0+5,front_z-0.2,11.4,9.5,0.18,'Glass')
        box('BalDoorMid',0,y0+5,front_z-0.35,0.18,9.5,0.32,'MetalDark')

# core connection to corridor, leave wide 12-stud opening at z=-9
box('CoreCorridorL',-10,7,cf,8,14,1,'WhiteBrick'); box('CoreCorridorR',10,7,cf,8,14,1,'WhiteBrick')
box('CoreCorridorHeader',0,12,cf,12,4,1,'WhiteBrick')
box('CoreCorridorL2',-10,21,cf,8,14,1,'WhiteBrick'); box('CoreCorridorR2',10,21,cf,8,14,1,'WhiteBrick')
box('CoreCorridorHeader2',0,26,cf,12,4,1,'WhiteBrick')

# staircase in core - real U shape, first flight toward facade, landing, return
stair_flight('StairA',-5.0,-13.0,-1,0,14,7.2,1.15,0.5)
box('StairLanding',0,7.0,-30.0,18,0.5,7.5,'Concrete')
stair_flight('StairB',5.0,-30.0,1,7.0,14,7.2,1.15,0.5)
# landings at bottom/top
box('StairBottomLanding',-5,0.15,-11,8,0.3,5,'Concrete')
box('StairTopLanding',5,14.15,-11,8,0.3,5,'Concrete')
# rails approximated with posts and rails along flights
for side,x in [('AL',-8.9),('AR',-1.1)]:
    for i in range(8):
        z=-14-i*2.0; y=2.0+i*0.65
        box(f'Rail{side}{i}',x,y,z,0.14,3.1,0.14,'MetalDark')
for side,x in [('BL',1.1),('BR',8.9)]:
    for i in range(8):
        z=-29+i*2.0; y=9.0+i*0.65
        box(f'Rail{side}{i}',x,y,z,0.14,3.1,0.14,'MetalDark')
# landing rails
box('LandingRailFront',0,8.8,-33.4,18,3.2,0.18,'MetalDark')

# balcony central at second floor
box('BalconySlab',0,14.15,-51.0,34,0.5,12,'Concrete')
# underside fascia
box('BalconyFascia',0,13.55,-56.7,34,1.2,0.6,'ConcreteLight')
# two structural columns
for x in (-13,13): box('BalconyColumn'+str(x),x,7,-52,1.6,14,1.6,'ConcreteLight')
# balcony railing with posts and horizontal bars
for x in range(-16,17,4): box('BalPost'+str(x),x,16.0,-56.6,0.18,3.4,0.18,'MetalDark')
box('BalTopRail',0,17.7,-56.6,34,0.18,0.22,'MetalDark')
box('BalMidRail',0,16.3,-56.6,34,0.14,0.18,'MetalDark')
for z in (-50,-53):
    box('BalSideL'+str(z),-16.8,16.0,z,0.18,3.4,0.18,'MetalDark'); box('BalSideR'+str(z),16.8,16.0,z,0.18,3.4,0.18,'MetalDark')
# entrance canopy below balcony
box('EntranceCanopy',0,10.8,-51.5,30,0.55,10,'ConcreteLight')
box('CanopyFascia',0,10.45,-56.25,30,0.7,0.5,'Green')
# central modern vertical fins / sign panel
box('SignPanel',0,24.5,-45.7,13,5.4,0.5,'Green')
for x in (-15.5,15.5): box('VerticalFin'+str(x),x,20.5,-46.0,0.8,15,1.4,'ConcreteLight')

# main gable roof with visible tile ribs
roof_base=28.0; run=49.0; rise=8.0; theta=math.atan2(rise,run); sl=math.hypot(run,rise)
# front and back roof slabs, overhang 3
box('RoofFront',0,roof_base+rise/2,-run/2,W+8,0.38,sl+3,'RoofTile',rx=-theta)
box('RoofBack',0,roof_base+rise/2,run/2,W+8,0.38,sl+3,'RoofTile',rx=theta)
# tile ribs down slope, visible geometry
for x in [i*2.6 for i in range(-35,36)]:
    if abs(x)<=92:
        box(f'RoofRibF_{x:.1f}',x,roof_base+rise/2+0.18,-run/2,0.16,0.14,sl+2,'RoofHighlight',rx=-theta)
        box(f'RoofRibB_{x:.1f}',x,roof_base+rise/2+0.18,run/2,0.16,0.14,sl+2,'RoofHighlight',rx=theta)
# horizontal tile rows across roof
for j,t in enumerate([i/12 for i in range(1,12)]):
    # front eave -> ridge interpolation
    z=-run + run*t; y=roof_base + rise*t
    box(f'RoofRowF{j}',0,y+0.16,z,W+7,0.13,0.22,'RoofHighlight',rx=-theta)
    z2=run-run*t; y2=roof_base+rise*t
    box(f'RoofRowB{j}',0,y2+0.16,z2,W+7,0.13,0.22,'RoofHighlight',rx=theta)
# ridge cap, thick visible
box('MainRidge',0,roof_base+rise+0.35,0,W+8,0.7,1.15,'RoofCap')
# fascia / gutters
box('FrontFascia',0,roof_base-0.05,-49.8,W+8,0.7,0.45,'RoofCap')
box('BackFascia',0,roof_base-0.05,49.8,W+8,0.7,0.45,'RoofCap')

# raised perpendicular central roof over entrance/core: gable slopes left-right, ridge along Z
core_base=30.2; halfw=18.5; cr=6.0; ctheta=math.atan2(cr,halfw); csl=math.hypot(cr,halfw); cz=-31.0; cdepth=55
# rotate around Z for slope along X
box('CoreRoofLeft',-halfw/2,core_base+cr/2,cz,csl+2,0.38,cdepth,'RoofTile',rz=ctheta)
box('CoreRoofRight',halfw/2,core_base+cr/2,cz,csl+2,0.38,cdepth,'RoofTile',rz=-ctheta)
# core roof tile ribs parallel down slope / along x, spaced along z
for z in range(-56,-5,3):
    box('CoreTileL'+str(z),-halfw/2,core_base+cr/2+0.17,z,csl+1,0.12,0.16,'RoofHighlight',rz=ctheta)
    box('CoreTileR'+str(z),halfw/2,core_base+cr/2+0.17,z,csl+1,0.12,0.16,'RoofHighlight',rz=-ctheta)
box('CoreRidge',0,core_base+cr+0.34,cz,1.1,0.7,cdepth+1,'RoofCap')

# exterior corner quoins / bands for more shape
for x in (-88,88):
    for y in (7,21): box(f'CornerBand{x}_{y}',x, y, front_z-0.65,1.6,12.5,1.1,'ConcreteLight')
# thin horizontal belt between floors
box('FrontBelt',0,14.05,front_z-0.58,W,0.35,0.18,'ConcreteLight')
box('BackBelt',0,14.05,back_z+0.58,W,0.35,0.18,'ConcreteLight')

# front steps / approach
for i in range(3):
    box(f'EntryStep{i}',0,0.12+i*0.16,-58.0-i*1.25,20-i*2,0.24,2.5,'Concrete')
box('FrontWalkway',0,-0.28,-68,36,0.18,18,'Paving')

# write files
mtl='''newmtl WhiteBrick\nKd 0.86 0.86 0.82\nKa 0.15 0.15 0.14\nNs 20\n\nnewmtl Green\nKd 0.10 0.24 0.15\nKa 0.04 0.08 0.05\nNs 30\n\nnewmtl Concrete\nKd 0.55 0.56 0.55\nKa 0.1 0.1 0.1\nNs 10\n\nnewmtl ConcreteLight\nKd 0.80 0.81 0.79\nKa 0.12 0.12 0.12\nNs 25\n\nnewmtl Floor\nKd 0.30 0.31 0.30\nKa 0.05 0.05 0.05\nNs 18\n\nnewmtl Glass\nKd 0.25 0.48 0.63\nKa 0.03 0.05 0.07\nd 0.35\nTr 0.65\nillum 4\nNs 80\n\nnewmtl MetalDark\nKd 0.06 0.08 0.07\nKa 0.02 0.02 0.02\nNs 70\n\nnewmtl RoofTile\nKd 0.43 0.20 0.10\nKa 0.10 0.04 0.02\nNs 18\n\nnewmtl RoofHighlight\nKd 0.62 0.31 0.16\nKa 0.12 0.06 0.03\nNs 24\n\nnewmtl RoofCap\nKd 0.30 0.12 0.07\nKa 0.08 0.03 0.02\nNs 28\n\nnewmtl LightPanel\nKd 0.92 0.93 0.90\nKa 0.25 0.25 0.22\nNs 100\n\nnewmtl Paving\nKd 0.38 0.39 0.38\nKa 0.08 0.08 0.08\nNs 12\n'''
with open(os.path.join(OUT,'BlocoInstrucao_V5.mtl'),'w',encoding='utf-8') as f:f.write(mtl)
with open(os.path.join(OUT,'BlocoInstrucao_V5.obj'),'w',encoding='utf-8') as f:
    f.write('mtllib BlocoInstrucao_V5.mtl\n# Bloco de Instrucao V5 - 2 andares, 8 salas por andar, corredor central, varanda e telhas geometrizadas\n')
    for v in verts: f.write(f'v {v[0]:.4f} {v[1]:.4f} {v[2]:.4f}\n')
    for line in obj: f.write(line+'\n')
print('generated',len(verts),'vertices')
