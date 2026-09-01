import math, os

OUT='models/v6'
os.makedirs(OUT, exist_ok=True)
obj=[]; verts=[]

def addv(v):
    verts.append(tuple(v)); return len(verts)

def use(name, mat):
    obj.append(f'o {name}'); obj.append(f'usemtl {mat}')

def box(name,cx,cy,cz,sx,sy,sz,mat='WhiteBrick',rx=0,ry=0,rz=0):
    use(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),(-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    cr,sr=math.cos(rx),math.sin(rx); cyy,syy=math.cos(ry),math.sin(ry); czr,szr=math.cos(rz),math.sin(rz)
    ids=[]
    for x,y,z in pts:
        y,z=y*cr-z*sr,y*sr+z*cr
        x,z=x*cyy+z*syy,-x*syy+z*cyy
        x,y=x*czr-y*szr,x*szr+y*czr
        ids.append(addv((cx+x,cy+y,cz+z)))
    for a,b,c,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        obj.append(f'f {ids[a]} {ids[b]} {ids[c]} {ids[d]}')

def quad(name,p1,p2,p3,p4,mat):
    use(name,mat); ids=[addv(p) for p in (p1,p2,p3,p4)]; obj.append('f '+' '.join(map(str,ids)))

def tri_prism_x(name,x1,x2,yz1,yz2,yz3,mat):
    use(name,mat)
    ids=[]
    for x in (x1,x2):
        for y,z in (yz1,yz2,yz3): ids.append(addv((x,y,z)))
    obj.append(f'f {ids[0]} {ids[1]} {ids[2]}')
    obj.append(f'f {ids[5]} {ids[4]} {ids[3]}')
    obj.append(f'f {ids[0]} {ids[3]} {ids[4]} {ids[1]}')
    obj.append(f'f {ids[1]} {ids[4]} {ids[5]} {ids[2]}')
    obj.append(f'f {ids[2]} {ids[5]} {ids[3]} {ids[0]}')

def tri_prism_z(name,z1,z2,xy1,xy2,xy3,mat):
    use(name,mat)
    ids=[]
    for z in (z1,z2):
        for x,y in (xy1,xy2,xy3): ids.append(addv((x,y,z)))
    obj.append(f'f {ids[0]} {ids[2]} {ids[1]}')
    obj.append(f'f {ids[3]} {ids[4]} {ids[5]}')
    obj.append(f'f {ids[0]} {ids[1]} {ids[4]} {ids[3]}')
    obj.append(f'f {ids[1]} {ids[2]} {ids[5]} {ids[4]}')
    obj.append(f'f {ids[2]} {ids[0]} {ids[3]} {ids[5]}')

def roof_panel_z(name,x1,x2,z1,y1,z2,y2,mat='RoofTile',th=0.28):
    use(name,mat)
    pts=[(x1,y1,z1),(x2,y1,z1),(x2,y2,z2),(x1,y2,z2),
         (x1,y1-th,z1),(x2,y1-th,z1),(x2,y2-th,z2),(x1,y2-th,z2)]
    ids=[addv(p) for p in pts]
    for a,b,c,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        obj.append(f'f {ids[a]} {ids[b]} {ids[c]} {ids[d]}')

def roof_panel_x(name,z1,z2,x1,y1,x2,y2,mat='RoofTile',th=0.28):
    use(name,mat)
    pts=[(x1,y1,z1),(x1,y1,z2),(x2,y2,z2),(x2,y2,z1),
         (x1,y1-th,z1),(x1,y1-th,z2),(x2,y2-th,z2),(x2,y2-th,z1)]
    ids=[addv(p) for p in pts]
    for a,b,c,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        obj.append(f'f {ids[a]} {ids[b]} {ids[c]} {ids[d]}')

# ------------------ dimensions ------------------
W=184.0; D=94.0; FH=14.0; H=28.0; COR=20.0
front_z=-47.0; back_z=47.0; cf=-10.0; cb=10.0
front_ranges=[(-92,-54),(-54,-16),(16,54),(54,92)]
back_ranges=[(-92,-46),(-46,0),(0,46),(46,92)]

# ------------------ openings ------------------
def frame_window(prefix,cx,y0,z,face,w=9.0,h=5.6,sill=4.0):
    sign=-1 if face=='front' else 1
    # glass sits through the wall so it is visible from inside the classroom too
    box(prefix+'_Glass',cx,y0+sill+h/2,z,w-0.55,h-0.55,0.24,'Glass')
    for dx in (-w/2,w/2): box(prefix+'_Jamb'+str(dx),cx+dx,y0+sill+h/2,z-sign*0.10,0.30,h+0.75,0.42,'MetalDark')
    box(prefix+'_Head',cx,y0+sill+h,z-sign*0.10,w+0.6,0.30,0.42,'MetalDark')
    box(prefix+'_SillFrame',cx,y0+sill,z-sign*0.10,w+0.6,0.30,0.42,'MetalDark')
    box(prefix+'_Mullion',cx,y0+sill+h/2,z,0.18,h-0.2,0.32,'MetalDark')
    # exterior sunshade and interior sill
    box(prefix+'_Shade',cx,y0+sill+h+0.48,z-sign*0.74,w+1.4,0.28,1.45,'ConcreteLight')
    box(prefix+'_InteriorSill',cx,y0+sill-0.20,z+sign*0.42,w+0.9,0.25,0.75,'ConcreteLight')

def exterior_room_wall(prefix,xa,xb,y0,z,face):
    w=9.0; h=5.6; sill=4.0; width=xb-xa; cx=(xa+xb)/2
    offs=width*0.24; wx=[cx-offs,cx+offs]
    box(prefix+'_Bottom',cx,y0+sill/2,z,width,sill,1,'WhiteBrick')
    top=FH-sill-h
    box(prefix+'_Top',cx,y0+sill+h+top/2,z,width,top,1,'WhiteBrick')
    segments=[(xa,wx[0]-w/2),(wx[0]+w/2,wx[1]-w/2),(wx[1]+w/2,xb)]
    for i,(a,b) in enumerate(segments):
        if b>a: box(prefix+f'_Pier{i}',(a+b)/2,y0+sill+h/2,z,b-a,h,1,'WhiteBrick')
    for i,x in enumerate(wx): frame_window(prefix+f'_Window{i+1}',x,y0,z,face,w,h,sill)
    sign=-1 if face=='front' else 1
    box(prefix+'_GreenPlinth',cx,y0+1.05,z-sign*0.55,width,2.1,0.10,'Green')

# door placed close to a classroom corner, not centered
# reference used: school/classroom examples commonly place entry at a front corner.
def classroom_door_wall(prefix,xa,xb,y0,z,side='left'):
    dw,dh=5.8,8.8; margin=2.0
    cx=xa+margin+dw/2 if side=='left' else xb-margin-dw/2
    if cx-dw/2>xa: box(prefix+'_WallL',(xa+cx-dw/2)/2,y0+FH/2,z,(cx-dw/2)-xa,FH,1,'WhiteBrick')
    if xb>cx+dw/2: box(prefix+'_WallR',(cx+dw/2+xb)/2,y0+FH/2,z,xb-(cx+dw/2),FH,1,'WhiteBrick')
    box(prefix+'_Header',cx,y0+dh+(FH-dh)/2,z,dw,FH-dh,1,'WhiteBrick')
    # actual classroom door leaf + narrow vision glass
    box(prefix+'_Door',cx,y0+dh/2,z, dw-0.32,dh-0.18,0.24,'DoorGreen')
    box(prefix+'_Vision',cx+0.95,y0+5.7,z-0.15,0.72,3.0,0.12,'Glass')
    box(prefix+'_FrameL',cx-dw/2,y0+dh/2,z-0.18,0.25,dh+0.45,0.40,'MetalDark')
    box(prefix+'_FrameR',cx+dw/2,y0+dh/2,z-0.18,0.25,dh+0.45,0.40,'MetalDark')
    box(prefix+'_FrameTop',cx,y0+dh,z-0.18,dw+0.5,0.25,0.40,'MetalDark')
    box(prefix+'_Plaque',cx+(3.7 if side=='left' else -3.7),y0+6.1,z-0.22,1.7,0.8,0.14,'Green')

# ------------------ base structure ------------------
box('Foundation',0,-0.55,0,W+8,1.1,D+8,'Concrete')
box('GroundFloor',0,-0.15,0,W,0.30,D,'Floor')
box('SecondFloor',0,13.75,0,W,0.50,D,'Concrete')
box('CeilingTop',0,27.78,0,W,0.44,D,'ConcreteLight')

# Side walls are split so corridor ends can have real windows.
def side_end(floor,x,side):
    y0=floor*FH
    box(f'Side_{side}_Front_A{floor+1}',x,y0+7,-28.5,1,14,37,'WhiteBrick')
    box(f'Side_{side}_Back_A{floor+1}',x,y0+7,28.5,1,14,37,'WhiteBrick')
    # corridor end window opening: lower, upper and side jambs
    sill=3.2; wh=6.4; ww=12.0
    box(f'Side_{side}_CorrBottom_A{floor+1}',x,y0+sill/2,0,1,sill,20,'WhiteBrick')
    top=14-sill-wh
    box(f'Side_{side}_CorrTop_A{floor+1}',x,y0+sill+wh+top/2,0,1,top,20,'WhiteBrick')
    box(f'Side_{side}_CorrJambF_A{floor+1}',x,y0+sill+wh/2,-8.0,1,wh,4,'WhiteBrick')
    box(f'Side_{side}_CorrJambB_A{floor+1}',x,y0+sill+wh/2,8.0,1,wh,4,'WhiteBrick')
    box(f'Side_{side}_CorrGlass_A{floor+1}',x,y0+sill+wh/2,0,0.22,wh-0.35,ww,'Glass')
    for z in (-6,6): box(f'Side_{side}_CorrFrame_{floor}_{z}',x+(0.14 if side=='L' else -0.14),y0+sill+wh/2,z,0.36,wh+0.6,0.24,'MetalDark')
    box(f'Side_{side}_CorrFrameMid_A{floor+1}',x+(0.14 if side=='L' else -0.14),y0+sill+wh/2,0,0.36,wh+0.6,0.18,'MetalDark')

for floor in (0,1):
    side_end(floor,-92,'L'); side_end(floor,92,'R')

# ------------------ classrooms ------------------
front_sides=['left','right','left','right']
back_sides=['left','left','right','right']
for floor in (0,1):
    y0=floor*FH
    for i,(xa,xb) in enumerate(front_ranges):
        exterior_room_wall(f'A{floor+1}_FrontRoom{i+1}',xa,xb,y0,front_z,'front')
        classroom_door_wall(f'A{floor+1}_FrontDoor{i+1}',xa,xb,y0,cf,front_sides[i])
    for i,(xa,xb) in enumerate(back_ranges):
        exterior_room_wall(f'A{floor+1}_BackRoom{i+1}',xa,xb,y0,back_z,'back')
        classroom_door_wall(f'A{floor+1}_BackDoor{i+1}',xa,xb,y0,cb,back_sides[i])
    # full-height classroom side partitions
    for x in (-54,-16,16,54):
        box(f'A{floor+1}_FrontDivider_{x}',x,y0+7,-28.5,1,14,37,'WhiteBrick')
    for x in (-46,0,46): box(f'A{floor+1}_BackDivider_{x}',x,y0+7,28.5,1,14,37,'WhiteBrick')
    # corridor detailing
    box(f'A{floor+1}_CorrSkirtFront',0,y0+0.65,cf+0.54,W,1.3,0.09,'Green')
    box(f'A{floor+1}_CorrSkirtBack',0,y0+0.65,cb-0.54,W,1.3,0.09,'Green')
    box(f'A{floor+1}_CorrCeiling',0,y0+13.58,0,W,0.18,COR,'Ceiling')
    for lx in (-78,-52,-26,0,26,52,78): box(f'A{floor+1}_CorrLight_{lx}',lx,y0+13.45,0,8.0,0.10,1.4,'LightPanel')

# facade pilasters and horizontal bands: gives depth instead of a flat box
for x in (-92,-54,-16,16,54,92):
    box(f'FrontPilaster_{x}',x,14,front_z-0.58,0.55,28,1.15,'ConcreteLight')
    box(f'BackPilaster_{x}',x,14,back_z+0.58,0.55,28,1.15,'ConcreteLight')
box('FrontFloorBand',0,14.1,front_z-0.62,W,0.48,0.28,'ConcreteLight')
box('BackFloorBand',0,14.1,back_z+0.62,W,0.48,0.28,'ConcreteLight')
box('FrontCornice',0,28.15,front_z-0.74,W+2,0.55,0.65,'ConcreteLight')
box('BackCornice',0,28.15,back_z+0.74,W+2,0.55,0.65,'ConcreteLight')

# ------------------ central entrance/hall ------------------
# ground entrance centered; upper level balcony door
for y0,name in ((0,'Entry'),(14,'BalconyPortal')):
    opening=12 if y0==0 else 9
    dh=10 if y0==0 else 9
    side=(32-opening)/2
    box(name+'_L',-opening/2-side/2,y0+7,front_z,side,14,1,'WhiteBrick')
    box(name+'_R', opening/2+side/2,y0+7,front_z,side,14,1,'WhiteBrick')
    box(name+'_Header',0,y0+dh+(14-dh)/2,front_z,opening,14-dh,1,'WhiteBrick')
    box(name+'_FrameL',-opening/2,y0+dh/2,front_z-0.22,0.32,dh+0.4,0.45,'MetalDark')
    box(name+'_FrameR', opening/2,y0+dh/2,front_z-0.22,0.32,dh+0.4,0.45,'MetalDark')
    box(name+'_FrameT',0,y0+dh,front_z-0.22,opening+0.5,0.32,0.45,'MetalDark')
    if y0==14:
        box('BalconyDoorLeaf',0,y0+4.5,front_z,opening-0.35,8.8,0.24,'DoorGreen')
        box('BalconyDoorGlass',0,y0+6.0,front_z-0.15,opening-2.0,3.4,0.12,'Glass')

# core side walls and corridor openings
for floor in (0,1):
    y0=floor*FH
    box(f'CoreWallL_A{floor+1}',-16,y0+7,-28.5,1,14,37,'WhiteBrick')
    box(f'CoreWallR_A{floor+1}',16,y0+7,-28.5,1,14,37,'WhiteBrick')
    # wide opening into the corridor, with portal frame
    box(f'CoreCorrL_A{floor+1}',-12,y0+7,cf,8,14,1,'WhiteBrick')
    box(f'CoreCorrR_A{floor+1}',12,y0+7,cf,8,14,1,'WhiteBrick')
    box(f'CoreCorrHead_A{floor+1}',0,y0+12.3,cf,16,3.4,1,'WhiteBrick')
    box(f'CorePortalBeam_A{floor+1}',0,y0+10.6,cf-0.65,17,0.45,1.2,'ConcreteLight')

# balcony and entrance composition
box('BalconySlab',0,14.15,-52.5,36,0.55,11,'Concrete')
box('BalconyFascia',0,13.55,-57.5,36,1.25,0.65,'ConcreteLight')
for x in (-14,14):
    box('BalconyColumn'+str(x),x,7,-53.5,1.8,14,1.8,'ConcreteLight')
    box('BalconyColumnGreen'+str(x),x,2.2,-54.46,2.0,4.4,0.10,'Green')
for x in range(-17,18,3): box('BalRailPost'+str(x),x,16.05,-57.8,0.18,3.5,0.18,'MetalDark')
box('BalRailTop',0,17.78,-57.8,36,0.20,0.25,'MetalDark')
box('BalRailMid',0,16.25,-57.8,36,0.15,0.20,'MetalDark')
for z in (-50,-54):
    box('BalRailSideL'+str(z),-17.8,16.7,z,0.20,3.1,6.5,'MetalDark')
    box('BalRailSideR'+str(z),17.8,16.7,z,0.20,3.1,6.5,'MetalDark')
# three broad entrance steps
for i in range(3):
    box(f'EntryStep{i+1}',0,0.20+i*0.22,-60.0+i*1.15,22-2*i,0.40,2.3,'ConcreteLight')
# blank sign panel / crest support above balcony
box('CentralSignPanel',0,24.8,front_z-0.72,13.5,4.8,0.45,'Green')
box('CentralSignInset',0,24.8,front_z-0.97,10.5,3.2,0.18,'ConcreteLight')

# ------------------ stair ------------------
def stair_flight(prefix,x,z0,zdir,y0,steps=14,width=7.8,run=1.12,rise=0.5):
    for i in range(steps):
        top=y0+(i+1)*rise; z=z0+zdir*(i+0.5)*run
        box(prefix+f'_Step{i+1}',x,top-rise/2,z,width,rise,run,'Concrete')

stair_flight('StairA',-5.0,-13.0,-1,0)
box('StairLanding',0,7.05,-30.0,18.5,0.55,7.8,'Concrete')
stair_flight('StairB',5.0,-30.0,1,7.0)
box('StairBottomLanding',-5,0.15,-11,8.2,0.3,5.0,'Concrete')
box('StairTopLanding',5,14.15,-11,8.2,0.3,5.0,'Concrete')
# stronger railing language
for lane,x,zstart,zdir,ybase in [('AL',-9.0,-13,-1,0),('AR',-1.0,-13,-1,0),('BL',1.0,-30,1,7),('BR',9.0,-30,1,7)]:
    for i in range(8):
        z=zstart+zdir*(1+i*2.0); y=ybase+1.8+i*0.82
        box(f'Rail_{lane}_{i}',x,y,z,0.16,3.2,0.16,'MetalDark')
box('LandingRail',0,8.8,-33.5,18.5,3.2,0.18,'MetalDark')

# ------------------ main roof ------------------
eave_y=28.75; ridge_y=35.4; eave_z=50.2
roof_panel_z('MainRoofFront',-95,95,-eave_z,eave_y,0,ridge_y,'RoofTile',0.34)
roof_panel_z('MainRoofBack',-95,95,eave_z,eave_y,0,ridge_y,'RoofTile',0.34)
# FILLED gable triangles: no open triangle under roof at the ends
tri_prism_x('GableLeft',-92.6,-91.8,(28,-47),(28,47),(35.4,0),'WhiteBrick')
tri_prism_x('GableRight',91.8,92.6,(28,-47),(35.4,0),(28,47),'WhiteBrick')
# ridge cap, fascias and gutters
box('MainRidgeCap',0,35.45,0,192,0.55,1.25,'RoofRidge')
box('FrontFascia',0,28.65,-50.4,192,0.85,0.55,'RoofDark')
box('BackFascia',0,28.65,50.4,192,0.85,0.55,'RoofDark')
box('FrontGutter',0,28.35,-50.85,192,0.32,0.42,'MetalDark')
box('BackGutter',0,28.35,50.85,192,0.32,0.42,'MetalDark')
# visible tile rows/ribs on both roof slopes
slope_ang=math.atan2(ridge_y-eave_y,eave_z)
for n,zabs in enumerate([2.4*i for i in range(1,21)]):
    if zabs>=eave_z-1: break
    t=zabs/eave_z
    y=ridge_y-(ridge_y-eave_y)*t
    box(f'TileRibFront_{n}',0,y+0.18,-zabs,191,0.16,0.42,'RoofRidge',rx=-slope_ang)
    box(f'TileRibBack_{n}',0,y+0.18,zabs,191,0.16,0.42,'RoofRidge',rx=slope_ang)
# downspouts
for x in (-82,-16,16,82):
    box('DownspoutF'+str(x),x,14,-50.75,0.35,28,0.35,'MetalDark')
    box('DownspoutB'+str(x),x,14,50.75,0.35,28,0.35,'MetalDark')

# ------------------ central cross-gable roof ------------------
# projecting, taller pavilion makes facade less flat and more modern-institutional
cross_z1=-58.8; cross_z2=-19.0; cross_eave=31.8; cross_ridge=38.2
roof_panel_x('CrossRoofLeft',cross_z1,cross_z2,-22,cross_eave,0,cross_ridge,'RoofTile',0.34)
roof_panel_x('CrossRoofRight',cross_z1,cross_z2,22,cross_eave,0,cross_ridge,'RoofTile',0.34)
tri_prism_z('CrossGableFront',cross_z1-0.45,cross_z1+0.45,(-22,cross_eave),(0,cross_ridge),(22,cross_eave),'WhiteBrick')
box('CrossRidge',0,cross_ridge+0.05,(cross_z1+cross_z2)/2,1.05,0.55,cross_z2-cross_z1,'RoofRidge')
# vertical fins at central pavilion
for x in (-18.5,18.5): box('CentralFin'+str(x),x,21.0,-48.0,0.75,14.0,2.2,'ConcreteLight')

# ------------------ minimal scale references ------------------
def chair(prefix,x,y,z,rot=0):
    # simple classroom chair with writing tablet
    box(prefix+'_Seat',x,y+2.0,z,2.2,0.24,2.0,'Chair',ry=rot)
    box(prefix+'_Back',x,y+3.2,z+0.90,2.2,2.0,0.22,'Chair',ry=rot)
    for dx in (-0.8,0.8):
        for dz in (-0.7,0.7): box(prefix+'_Leg',x+dx,y+1.0,z+dz,0.14,2.0,0.14,'MetalDark')
    box(prefix+'_TabletSupport',x+1.15,y+2.65,z-0.1,0.14,1.5,0.14,'MetalDark')
    box(prefix+'_Tablet',x+1.3,y+3.4,z-0.25,1.55,0.12,1.9,'Wood')

for floor in (0,1):
    y=floor*FH
    for i,(xa,xb) in enumerate(front_ranges): chair(f'Chair_F_{floor}_{i}',(xa+xb)/2,y,-29)
    for i,(xa,xb) in enumerate(back_ranges): chair(f'Chair_B_{floor}_{i}',(xa+xb)/2,y,29,math.pi)

# ------------------ write files ------------------
obj_text='mtllib BlocoInstrucao_V6.mtl\n# Bloco de Instrucao V6 - detailed EB-inspired training building\n' + '\n'.join(f'v {x:.5f} {y:.5f} {z:.5f}' for x,y,z in verts) + '\n' + '\n'.join(obj) + '\n'
with open(os.path.join(OUT,'BlocoInstrucao_V6.obj'),'w',encoding='utf-8') as f: f.write(obj_text)

mtl='''newmtl WhiteBrick
Kd 0.83 0.84 0.80
Ka 0.10 0.10 0.09
Ns 20

newmtl Concrete
Kd 0.46 0.47 0.46
Ns 10

newmtl ConcreteLight
Kd 0.73 0.74 0.70
Ns 20

newmtl Floor
Kd 0.30 0.31 0.30
Ns 30

newmtl Ceiling
Kd 0.86 0.86 0.83
Ns 10

newmtl Green
Kd 0.08 0.21 0.12
Ns 30

newmtl DoorGreen
Kd 0.07 0.18 0.10
Ns 40

newmtl MetalDark
Kd 0.08 0.09 0.09
Ns 70

newmtl Glass
Kd 0.35 0.60 0.72
d 0.38
Tr 0.62
Ns 90

newmtl RoofTile
Kd 0.43 0.19 0.10
Ns 25

newmtl RoofRidge
Kd 0.30 0.12 0.07
Ns 25

newmtl RoofDark
Kd 0.20 0.12 0.09
Ns 25

newmtl LightPanel
Kd 0.96 0.96 0.88
Ke 0.55 0.55 0.48
Ns 20

newmtl Chair
Kd 0.06 0.065 0.06
Ns 40

newmtl Wood
Kd 0.42 0.25 0.12
Ns 25
'''
with open(os.path.join(OUT,'BlocoInstrucao_V6.mtl'),'w',encoding='utf-8') as f: f.write(mtl)

readme='''BLOCO DE INSTRUCAO V6\n\n- 2 andares\n- 8 salas por andar / 16 salas no total\n- portas de sala deslocadas para perto dos cantos\n- duas janelas reais na parede externa de cada sala, visiveis pelo interior\n- triangulos de empena preenchidos sob o telhado\n- telhado de duas aguas com fileiras/ripas visiveis, cumeeira, fascia, calha e descidas\n- pavilhao central com telhado cruzado, varanda e entrada destacada\n- corredor interno fechado com janelas nas extremidades\n- uma cadeira de escala em cada sala\n\nOBJ usa grupos nomeados para facilitar futura conversao em Parts/MeshParts.\n'''
with open(os.path.join(OUT,'README_V6.txt'),'w',encoding='utf-8') as f: f.write(readme)
print('generated',len(verts),'vertices')
