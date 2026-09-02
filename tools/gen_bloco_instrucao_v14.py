import os, math, random
from pathlib import Path
from PIL import Image, ImageDraw

OUT=Path('models/v14')
TEX=OUT/'textures'
TEX.mkdir(parents=True, exist_ok=True)
obj=[]
v_count=0
vt_count=0

# ------------------------------------------------------------
# TEXTURES: all architectural surface detail is baked here.
# No modeled brick courses / roof tile ribs / fake relief.
# ------------------------------------------------------------
def save_wall(path):
    im=Image.new('RGB',(256,256),(238,239,235)); d=ImageDraw.Draw(im)
    for y in range(0,257,32): d.line((0,y,256,y), fill=(213,215,210), width=2)
    row=0
    for y in range(0,256,32):
        off=0 if row%2==0 else 32
        for x in range(off,257,64): d.line((x,y,x,y+32), fill=(219,221,216), width=2)
        row+=1
    # very subtle painted variation
    px=im.load(); random.seed(14)
    for _ in range(2500):
        x=random.randrange(256); y=random.randrange(256); c=random.choice((-3,-2,2,3))
        r,g,b=px[x,y]; px[x,y]=(max(0,min(255,r+c)),max(0,min(255,g+c)),max(0,min(255,b+c)))
    im.save(path)

def save_roof(path):
    im=Image.new('RGB',(256,256),(78,79,76)); d=ImageDraw.Draw(im)
    for y in range(0,257,28):
        d.line((0,y,256,y), fill=(53,54,52), width=2)
        row=y//28; off=0 if row%2==0 else 24
        for x in range(off,257,48): d.line((x,y,x,y+28), fill=(65,66,63), width=2)
    # small highlight under every course to fake tile depth while mesh stays flat
    for y in range(2,257,28): d.line((0,y,256,y), fill=(96,97,92), width=1)
    im.save(path)

def save_concrete(path, base=(188,190,187)):
    im=Image.new('RGB',(256,256),base); px=im.load(); random.seed(22)
    for _ in range(9000):
        x=random.randrange(256); y=random.randrange(256); c=random.choice((-7,-4,-2,2,4,6))
        r,g,b=px[x,y]; px[x,y]=(max(0,min(255,r+c)),max(0,min(255,g+c)),max(0,min(255,b+c)))
    im.save(path)

def save_floor(path):
    im=Image.new('RGB',(256,256),(179,181,178)); d=ImageDraw.Draw(im)
    for q in range(0,257,64):
        d.line((q,0,q,256),fill=(145,147,145),width=2); d.line((0,q,256,q),fill=(145,147,145),width=2)
    im.save(path)

def save_green(path):
    im=Image.new('RGB',(256,256),(57,88,60)); px=im.load(); random.seed(44)
    for _ in range(2800):
        x=random.randrange(256); y=random.randrange(256); c=random.choice((-4,-2,2,3))
        r,g,b=px[x,y]; px[x,y]=(max(0,min(255,r+c)),max(0,min(255,g+c)),max(0,min(255,b+c)))
    im.save(path)

def save_metal(path):
    im=Image.new('RGB',(128,128),(69,72,72)); d=ImageDraw.Draw(im)
    for x in range(0,128,8): d.line((x,0,x,128),fill=(74,77,77),width=1)
    im.save(path)

save_wall(TEX/'wall_brick.png')
save_roof(TEX/'roof_tiles.png')
save_concrete(TEX/'concrete.png')
save_floor(TEX/'floor.png')
save_green(TEX/'green_paint.png')
save_metal(TEX/'metal.png')

# ------------------------------------------------------------
# OBJ helpers with tiled UVs.
# ------------------------------------------------------------
def begin(name,mat):
    obj.append(f'o {name}')
    obj.append(f'usemtl {mat}')

def face(points, ulen=1.0, vlen=1.0, tile=4.0):
    global v_count, vt_count
    vids=[]; tids=[]
    for p in points:
        v_count+=1; vids.append(v_count); obj.append('v %.5f %.5f %.5f'%p)
    uv=[(0,0),(ulen/tile,0),(ulen/tile,vlen/tile),(0,vlen/tile)]
    for u,v in uv:
        vt_count+=1; tids.append(vt_count); obj.append('vt %.5f %.5f'%(u,v))
    obj.append('f '+' '.join(f'{vi}/{ti}' for vi,ti in zip(vids,tids)))

def transform(p,cx,cy,cz,rx=0,ry=0,rz=0):
    x,y,z=p
    cr,sr=math.cos(rx),math.sin(rx); cyy,syy=math.cos(ry),math.sin(ry); czr,szr=math.cos(rz),math.sin(rz)
    y,z=y*cr-z*sr,y*sr+z*cr
    x,z=x*cyy+z*syy,-x*syy+z*cyy
    x,y=x*czr-y*szr,x*szr+y*czr
    return (cx+x,cy+y,cz+z)

def box(name,cx,cy,cz,sx,sy,sz,mat='Wall',rx=0,ry=0,rz=0,tile=4.0):
    begin(name,mat); hx,hy,hz=sx/2,sy/2,sz/2
    p=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),(-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    p=[transform(q,cx,cy,cz,rx,ry,rz) for q in p]
    # z-normal faces: width x height
    face([p[0],p[1],p[2],p[3]],sx,sy,tile); face([p[5],p[4],p[7],p[6]],sx,sy,tile)
    # y-normal faces: width x depth
    face([p[4],p[5],p[1],p[0]],sx,sz,tile); face([p[3],p[2],p[6],p[7]],sx,sz,tile)
    # x-normal faces: depth x height
    face([p[4],p[0],p[3],p[7]],sz,sy,tile); face([p[1],p[5],p[6],p[2]],sz,sy,tile)

def tri_prism_x(name,x1,x2,yz1,yz2,yz3,mat='Wall'):
    begin(name,mat)
    a=[(x1,*yz1),(x1,*yz2),(x1,*yz3)]
    b=[(x2,*yz1),(x2,*yz2),(x2,*yz3)]
    # triangles use degenerate 4th point only for UV helper avoidance: explicit triangle function below
    def tri(points):
        global v_count,vt_count
        vids=[]; tids=[]
        for p in points:
            v_count+=1; vids.append(v_count); obj.append('v %.5f %.5f %.5f'%p)
        for uv in ((0,0),(1,0),(0.5,1)):
            vt_count+=1; tids.append(vt_count); obj.append('vt %.5f %.5f'%uv)
        obj.append('f '+' '.join(f'{vi}/{ti}' for vi,ti in zip(vids,tids)))
    tri([a[0],a[1],a[2]]); tri([b[2],b[1],b[0]])
    face([a[0],b[0],b[1],a[1]],abs(x2-x1),math.dist(yz1,yz2),4)
    face([a[1],b[1],b[2],a[2]],abs(x2-x1),math.dist(yz2,yz3),4)
    face([a[2],b[2],b[0],a[0]],abs(x2-x1),math.dist(yz3,yz1),4)

def roof_panel(name,x1,x2,z_eave,z_ridge,y_eave,y_ridge,front=True):
    # thin, perfectly smooth roof half. Tile appearance is texture only.
    th=0.30; begin(name,'Roof')
    z1=z_eave; z2=z_ridge
    pts=[(x1,y_eave,z1),(x2,y_eave,z1),(x2,y_ridge,z2),(x1,y_ridge,z2),
         (x1,y_eave-th,z1),(x2,y_eave-th,z1),(x2,y_ridge-th,z2),(x1,y_ridge-th,z2)]
    slope=math.hypot(z2-z1,y_ridge-y_eave)
    face([pts[0],pts[1],pts[2],pts[3]],x2-x1,slope,5)
    face([pts[5],pts[4],pts[7],pts[6]],x2-x1,slope,5)
    face([pts[4],pts[5],pts[1],pts[0]],x2-x1,th,4)
    face([pts[3],pts[2],pts[6],pts[7]],x2-x1,th,4)
    face([pts[4],pts[0],pts[3],pts[7]],slope,th,4)
    face([pts[1],pts[5],pts[6],pts[2]],slope,th,4)

def beam_between(name,x,z1,y1,z2,y2,width=0.22,height=0.22,mat='Metal'):
    dz=z2-z1; dy=y2-y1; length=math.hypot(dz,dy)
    angle=-math.atan2(dy,dz)
    box(name,x,(y1+y2)/2,(z1+z2)/2,width,height,length,mat,rx=angle,tile=2)

# ------------------------------------------------------------
# dimensions / layout
# ------------------------------------------------------------
W=184.0; D=94.0; FH=14.0; H=28.0
front_z=-47.0; back_z=47.0; cf=-10.0; cb=10.0
front_ranges=[(-92,-54),(-54,-16),(16,54),(54,92)]
back_ranges=[(-92,-46),(-46,0),(0,46),(46,92)]

# Floors
box('Foundation',0,-0.55,0,W+6,1.1,D+6,'Concrete',tile=8)
box('GroundFloor',0,-0.14,0,W,0.28,D,'Floor',tile=8)
# Second floor with real stair opening x[-11,11] z[-33,-9]
hx1,hx2=-11,11; hz1,hz2=-33,-9
box('SecondFloor_Left',(-92+hx1)/2,13.76,0,hx1+92,0.48,D,'Floor',tile=8)
box('SecondFloor_Right',(hx2+92)/2,13.76,0,92-hx2,0.48,D,'Floor',tile=8)
box('SecondFloor_Front',0,13.76,(-47+hz1)/2,hx2-hx1,0.48,hz1+47,'Floor',tile=8)
box('SecondFloor_Back',0,13.76,(hz2+47)/2,hx2-hx1,0.48,47-hz2,'Floor',tile=8)
box('TopCeiling',0,27.80,0,W,0.40,D,'Concrete',tile=8)

# ------------------------------------------------------------
# window / wall functions
# ------------------------------------------------------------
def flat_glass(name,cx,cy,cz,sx,sy,sz):
    box(name,cx,cy,cz,sx,sy,sz,'Glass',tile=8)

def ext_room_wall(prefix,xa,xb,y0,z,front=True):
    width=xb-xa; cx=(xa+xb)/2; sill=3.8; wh=5.8; ww=9.2
    wx=[cx-width*0.24,cx+width*0.24]
    box(prefix+'_Bottom',cx,y0+sill/2,z,width,sill,1,'Wall')
    top=FH-sill-wh
    box(prefix+'_Top',cx,y0+sill+wh+top/2,z,width,top,1,'Wall')
    cursor=xa
    for i,wc in enumerate(wx):
        a=wc-ww/2; b=wc+ww/2
        if a>cursor: box(prefix+f'_Pier{i}',(cursor+a)/2,y0+sill+wh/2,z,a-cursor,wh,1,'Wall')
        cursor=b
    if cursor<xb: box(prefix+'_PierEnd',(cursor+xb)/2,y0+sill+wh/2,z,xb-cursor,wh,1,'Wall')
    for i,wc in enumerate(wx):
        flat_glass(prefix+f'_Window{i+1}',wc,y0+sill+wh/2,z,ww-0.28,wh-0.28,0.16)
    # flat painted stripe: practically flush, no climbable relief
    sign=-1 if front else 1
    box(prefix+'_GreenStripe',cx,y0+1.0,z-sign*0.506,width,2.0,0.012,'Green',tile=6)

def corridor_room_wall(prefix,xa,xb,y0,z,door_side='left',front=True):
    Hh=FH; dw=5.8; dh=9.0; ww=10.5; wh=5.3; sill=3.6; margin=2.1; gap=2.0
    if door_side=='left':
        dl=xa+margin; dr=dl+dw; wl=dr+gap; wr=wl+ww
    else:
        dr=xb-margin; dl=dr-dw; wr=dl-gap; wl=wr-ww
    # full-height sections outside openings
    cuts=sorted({xa,dl,dr,wl,wr,xb})
    for i,(a,b) in enumerate(zip(cuts[:-1],cuts[1:])):
        mid=(a+b)/2
        if (dl<mid<dr) or (wl<mid<wr): continue
        box(prefix+f'_Solid{i}',mid,y0+Hh/2,z,b-a,Hh,1,'Wall')
    # over doorway
    box(prefix+'_DoorHeader',(dl+dr)/2,y0+dh+(Hh-dh)/2,z,dw,Hh-dh,1,'Wall')
    # window bottom/top around observation opening
    box(prefix+'_ObsBottom',(wl+wr)/2,y0+sill/2,z,ww,sill,1,'Wall')
    top=Hh-sill-wh
    box(prefix+'_ObsTop',(wl+wr)/2,y0+sill+wh+top/2,z,ww,top,1,'Wall')
    flat_glass(prefix+'_ObsGlass',(wl+wr)/2,y0+sill+wh/2,z,ww-0.30,wh-0.30,0.16)

def side_zone_wall(prefix,x,y0,za,zb,side,centers,ww=9.0,wh=5.6,sill=3.8):
    Hh=FH; zc=(za+zb)/2; width=zb-za
    box(prefix+'_Bottom',x,y0+sill/2,zc,1,sill,width,'Wall')
    top=Hh-sill-wh; box(prefix+'_Top',x,y0+sill+wh+top/2,zc,1,top,width,'Wall')
    cursor=za
    for i,wc in enumerate(sorted(centers)):
        a=wc-ww/2; b=wc+ww/2
        if a>cursor: box(prefix+f'_Pier{i}',x,y0+sill+wh/2,(cursor+a)/2,1,wh,a-cursor,'Wall')
        cursor=b
    if cursor<zb: box(prefix+'_PierEnd',x,y0+sill+wh/2,(cursor+zb)/2,1,wh,zb-cursor,'Wall')
    for i,wc in enumerate(centers): flat_glass(prefix+f'_Window{i+1}',x,y0+sill+wh/2,wc,0.16,wh-0.28,ww-0.28)

# classrooms / corridor walls
front_sides=['left','right','left','right']
back_sides=['left','left','right','right']
for floor in (0,1):
    y0=floor*FH
    for i,(xa,xb) in enumerate(front_ranges):
        ext_room_wall(f'A{floor+1}_FrontRoom{i+1}',xa,xb,y0,front_z,True)
        corridor_room_wall(f'A{floor+1}_FrontCorrWall{i+1}',xa,xb,y0,cf,front_sides[i],True)
    for i,(xa,xb) in enumerate(back_ranges):
        ext_room_wall(f'A{floor+1}_BackRoom{i+1}',xa,xb,y0,back_z,False)
        corridor_room_wall(f'A{floor+1}_BackCorrWall{i+1}',xa,xb,y0,cb,back_sides[i],False)
    for x in (-54,-16,16,54): box(f'A{floor+1}_FrontDivider_{x}',x,y0+7,-28.5,1,FH,37,'Wall')
    for x in (-46,0,46): box(f'A{floor+1}_BackDivider_{x}',x,y0+7,28.5,1,FH,37,'Wall')
    # side facades: actual holes, smooth wall surface
    for side,x in [('L',-92),('R',92)]:
        side_zone_wall(f'A{floor+1}_{side}_FrontSide',x,y0,-47,-10,side,[-37,-20],ww=7.5)
        side_zone_wall(f'A{floor+1}_{side}_CorrSide',x,y0,-10,10,side,[0],ww=10.5,wh=6.0,sill=3.4)
        side_zone_wall(f'A{floor+1}_{side}_BackSide',x,y0,10,47,side,[20,37],ww=7.5)

# Central front stair hall / entrance bay (the -16..16 gap in front rooms)
def central_front_floor(y0,upper=False):
    z=front_z
    if not upper:
        opening=12.0; oh=10.2
        box('Entrance_Left',(-16-opening/2)/2,y0+FH/2,z,16-opening/2,FH,1,'Wall')
        box('Entrance_Right',(opening/2+16)/2,y0+FH/2,z,16-opening/2,FH,1,'Wall')
        box('Entrance_Header',0,y0+oh+(FH-oh)/2,z,opening,FH-oh,1,'Wall')
    else:
        # wide upper hall window looking onto balcony
        sill=3.3; wh=6.4; ww=17.0
        box('UpperHall_Bottom',0,y0+sill/2,z,32,sill,1,'Wall')
        box('UpperHall_Top',0,y0+sill+wh+(FH-sill-wh)/2,z,32,FH-sill-wh,1,'Wall')
        box('UpperHall_Left',(-16-ww/2)/2,y0+sill+wh/2,z,16-ww/2,wh,1,'Wall')
        box('UpperHall_Right',((ww/2)+16)/2,y0+sill+wh/2,z,16-ww/2,wh,1,'Wall')
        flat_glass('UpperHall_Window',0,y0+sill+wh/2,z,ww-0.3,wh-0.3,0.16)
central_front_floor(0,False); central_front_floor(14,True)

# Balcony / central facade: simple and smooth, no decorative clutter.
box('CentralBalcony',0,14.10,-51.0,26,0.50,8.0,'Concrete',tile=6)
for x in (-12,12): box(f'BalconyPost_{x}',x,16.0,-54.5,0.22,3.8,0.22,'Metal')
for x in (-8,-4,0,4,8): box(f'BalconyPost_{x}',x,16.0,-54.5,0.18,3.8,0.18,'Metal')
box('BalconyRailTop',0,17.8,-54.5,24.3,0.20,0.20,'Metal')
box('BalconyRailMid',0,16.25,-54.5,24.3,0.16,0.16,'Metal')
box('EntranceCanopy',0,11.0,-51.7,18.0,0.35,5.4,'Roof',tile=5)

# ------------------------------------------------------------
# Staircase: conventional U stair, correctly fitted inside central core.
# No curved gimmick, no floor cutting through it.
# ------------------------------------------------------------
STEP_RISE=0.50; STEP_RUN=1.00; STEPS=14; FW=7.6
AX=-5.0; BX=5.0; START=-11.8; LAND_Z=-29.3; LAND_Y=7.0
# lower flight treads
for i in range(STEPS):
    top=(i+1)*STEP_RISE; z=START-(i+0.5)*STEP_RUN
    box(f'StairA_Tread_{i+1}',AX,top-0.16,z,FW,0.32,STEP_RUN+0.04,'Concrete',tile=5)
# smooth under-flight stringers/supports
beam_between('StairA_StringerOuter',AX-FW/2+0.30,START,0.15,START-STEPS*STEP_RUN,LAND_Y-0.20,0.34,0.48,'Concrete')
beam_between('StairA_StringerInner',AX+FW/2-0.30,START,0.15,START-STEPS*STEP_RUN,LAND_Y-0.20,0.34,0.48,'Concrete')
# broad square landing, properly connected
box('StairMidLanding',0,LAND_Y-0.20,LAND_Z,20.0,0.40,5.8,'Concrete',tile=5)
# upper flight starts at front edge of landing and returns to hall
BSTART=LAND_Z+2.4
for i in range(STEPS):
    top=LAND_Y+(i+1)*STEP_RISE; z=BSTART+(i+0.5)*STEP_RUN
    box(f'StairB_Tread_{i+1}',BX,top-0.16,z,FW,0.32,STEP_RUN+0.04,'Concrete',tile=5)
beam_between('StairB_StringerOuter',BX-FW/2+0.30,BSTART,LAND_Y+0.15,BSTART+STEPS*STEP_RUN,13.85,0.34,0.48,'Concrete')
beam_between('StairB_StringerInner',BX+FW/2-0.30,BSTART,LAND_Y+0.15,BSTART+STEPS*STEP_RUN,13.85,0.34,0.48,'Concrete')
box('StairBottomLanding',AX,0.14,-10.3,8.5,0.28,3.0,'Concrete')
box('StairTopLanding',BX,14.02,-10.1,8.5,0.36,3.4,'Concrete')

# Clean rails, fewer bars.
RH=3.1
for idx in range(0,STEPS+1,3):
    z=START-idx*STEP_RUN; y=idx*STEP_RISE+RH/2
    for x in (AX-FW/2-0.18,AX+FW/2+0.18): box(f'RailA_Post_{idx}_{x}',x,y,z,0.16,RH,0.16,'Metal')
for x in (AX-FW/2-0.18,AX+FW/2+0.18): beam_between(f'RailA_Top_{x}',x,START,RH,START-STEPS*STEP_RUN,LAND_Y+RH,0.18,0.18,'Metal')
for idx in range(0,STEPS+1,3):
    z=BSTART+idx*STEP_RUN; y=LAND_Y+idx*STEP_RISE+RH/2
    for x in (BX-FW/2-0.18,BX+FW/2+0.18): box(f'RailB_Post_{idx}_{x}',x,y,z,0.16,RH,0.16,'Metal')
for x in (BX-FW/2-0.18,BX+FW/2+0.18): beam_between(f'RailB_Top_{x}',x,BSTART,LAND_Y+RH,BSTART+STEPS*STEP_RUN,14+RH,0.18,0.18,'Metal')
# landing rail at back curve-free turnaround
for x in (-9,-6,-3,0,3,6,9): box(f'LandingRailPost_{x}',x,LAND_Y+RH/2,LAND_Z-2.7,0.16,RH,0.16,'Metal')
box('LandingRailTop',0,LAND_Y+RH,LAND_Z-2.7,18.2,0.18,0.18,'Metal')
# upper floor guard around opening, leaving exit at back
for x in (-10.6,10.6):
    box(f'UpperGuard_{x}',x,15.55,-21.1,0.18,3.1,22.5,'Metal')
box('UpperGuardFront',0,15.55,-32.5,21.2,3.1,0.18,'Metal')

# ------------------------------------------------------------
# Roof: one clean main gable roof. NO intersecting mini-roofs.
# This removes the previous holes / floating pieces / noisy tile geometry.
# ------------------------------------------------------------
EAVE_Y=28.45; RIDGE_Y=35.6; EAVE_Z=50.2
roof_panel('RoofFront',-94.0,94.0,-EAVE_Z,0.0,EAVE_Y,RIDGE_Y,True)
roof_panel('RoofBack',-94.0,94.0,EAVE_Z,0.0,EAVE_Y,RIDGE_Y,False)
# close BOTH end gables solidly to roof slope
tri_prism_x('GableLeft',-92.55,-91.75,(28.0,-47.0),(28.0,47.0),(35.6,0.0),'Wall')
tri_prism_x('GableRight',91.75,92.55,(28.0,-47.0),(35.6,0.0),(28.0,47.0),'Wall')
# smooth ridge cap only
box('RoofRidge',0,RIDGE_Y+0.05,0,188,0.30,0.65,'Roof',tile=5)

# A small flat central crest/sign panel, not a second roof.
box('CentralCrestPanel',0,29.8,-47.55,18.0,2.4,0.16,'Green',tile=5)

# ------------------------------------------------------------
# materials / output
# ------------------------------------------------------------
mtl='''# Bloco Instrucao V14 - smooth geometry + texture detail\n\nnewmtl Wall\nKa 0.80 0.80 0.78\nKd 1.00 1.00 1.00\nKs 0.03 0.03 0.03\nNs 8\nmap_Kd textures/wall_brick.png\n\nnewmtl Roof\nKa 0.35 0.35 0.34\nKd 0.78 0.78 0.76\nKs 0.12 0.12 0.12\nNs 20\nmap_Kd textures/roof_tiles.png\n\nnewmtl Concrete\nKa 0.55 0.55 0.54\nKd 0.85 0.85 0.83\nKs 0.04 0.04 0.04\nNs 8\nmap_Kd textures/concrete.png\n\nnewmtl Floor\nKa 0.45 0.45 0.44\nKd 0.82 0.82 0.80\nKs 0.06 0.06 0.06\nNs 12\nmap_Kd textures/floor.png\n\nnewmtl Green\nKa 0.20 0.30 0.21\nKd 0.62 0.72 0.62\nKs 0.03 0.03 0.03\nNs 6\nmap_Kd textures/green_paint.png\n\nnewmtl Metal\nKa 0.20 0.21 0.21\nKd 0.55 0.57 0.57\nKs 0.35 0.35 0.35\nNs 45\nmap_Kd textures/metal.png\n\nnewmtl Glass\nKa 0.14 0.18 0.20\nKd 0.50 0.64 0.70\nKs 0.55 0.55 0.55\nNs 80\nd 0.42\nillum 4\n'''

obj_path=OUT/'BlocoInstrucao_V14.obj'
with obj_path.open('w',encoding='utf-8') as f:
    f.write('mtllib BlocoInstrucao_V14.mtl\n')
    f.write('# V14 rebuilt from scratch: clean construction, smooth geometry, texture-driven detail\n')
    f.write('\n'.join(obj))
    f.write('\n')
(OUT/'BlocoInstrucao_V14.mtl').write_text(mtl,encoding='utf-8')
(OUT/'README_V14.txt').write_text('''BLOCO DE INSTRUCAO V14\n\nREFEITO DO ZERO, SEM HERDAR A GEOMETRIA ACUMULADA DAS VERSOES ANTERIORES.\n\n- Geometria lisa: sem tijolos fisicos, sem fileiras de telhas modeladas e sem relevo excessivo.\n- Tijolo branco, telhas, concreto, piso, metal e pintura verde sao TEXTURAS.\n- Telhado principal agora e uma unica cobertura de duas aguas, limpa, sem intersecoes quebradas.\n- Empenas laterais fecham corretamente ate o telhado.\n- 8 salas por andar / 16 no total.\n- 2 janelas externas por sala e janelas de observacao para o corredor.\n- Entradas das salas continuam como vaos abertos perto do canto, sem folhas de porta.\n- Escadaria refeita como U convencional com patamar central, vao real no segundo piso e guardas.\n- Varanda central mantida, com fachada simplificada e lisa.\n- OBJ + MTL + texturas + GLB no pacote final.\n''',encoding='utf-8')
print('V14 generated:',obj_path)
