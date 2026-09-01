from __future__ import annotations
import json, math, os, zipfile
from pathlib import Path

OUT = Path('generated/bloco_instrucao_3d')
OUT.mkdir(parents=True, exist_ok=True)

# --- escala em studs ---
ROOM_W, ROOM_D = 44.0, 40.0
CORRIDOR_W, CORE_W = 18.0, 36.0
FLOOR_H, FLOORS = 14.0, 2
BUILDING_W = ROOM_W*4 + CORE_W   # 212
BUILDING_D = ROOM_D*2 + CORRIDOR_W  # 98
WALL = 1.0
DOOR_W, DOOR_H = 6.0, 9.0
WINDOW_W, WINDOW_H, SILL = 7.0, 5.0, 4.0
FRONT_Z, BACK_Z = -BUILDING_D/2, BUILDING_D/2
CORR_F, CORR_B = -CORRIDOR_W/2, CORRIDOR_W/2
ROOM_X = (-84.0, -40.0, 40.0, 84.0)

MAT = {
    'white':'f2f0e8', 'concrete':'aaa9a4', 'roof':'9b5735', 'metal':'353a38',
    'glass':'82b3c7', 'wood':'8a5d39', 'chair':'242625', 'green':'344f3c',
    'skin':'d7b58a', 'uniform':'40553d'
}

verts=[]; faces=[]; objects=[]

def v(x,y,z):
    verts.append((float(x),float(y),float(z))); return len(verts)

def face(ids, mat, group):
    faces.append((tuple(ids),mat,group))

def add_box(name, cx,cy,cz, sx,sy,sz, mat='white'):
    x0,x1=cx-sx/2,cx+sx/2; y0,y1=cy-sy/2,cy+sy/2; z0,z1=cz-sz/2,cz+sz/2
    ids=[v(x0,y0,z0),v(x1,y0,z0),v(x1,y1,z0),v(x0,y1,z0),v(x0,y0,z1),v(x1,y0,z1),v(x1,y1,z1),v(x0,y1,z1)]
    for f in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]: face([ids[i] for i in f],mat,name)
    objects.append({'name':name,'kind':'box','center':[cx,cy,cz],'size':[sx,sy,sz],'mat':mat})

def add_panel(name, corners_top, thickness=0.35, mat='roof'):
    # painel inclinado: segunda pele deslocada apenas para baixo; suficiente para maquete/OBJ
    top=[v(*p) for p in corners_top]
    bot=[v(p[0],p[1]-thickness,p[2]) for p in corners_top]
    face(top,mat,name); face(list(reversed(bot)),mat,name)
    for i in range(4): face([top[i],top[(i+1)%4],bot[(i+1)%4],bot[i]],mat,name)
    objects.append({'name':name,'kind':'panel','mat':mat})

def add_wall_with_door(name,cx,y0,z,width,depth=WALL,mat='white'):
    side=(width-DOOR_W)/2
    add_box(name+'_L',cx-DOOR_W/2-side/2,y0+FLOOR_H/2,z,side,FLOOR_H,depth,mat)
    add_box(name+'_R',cx+DOOR_W/2+side/2,y0+FLOOR_H/2,z,side,FLOOR_H,depth,mat)
    add_box(name+'_H',cx,y0+DOOR_H+(FLOOR_H-DOOR_H)/2,z,DOOR_W,FLOOR_H-DOOR_H,depth,mat)

def add_wall_two_windows(name,cx,y0,z,width=ROOM_W,mat='white',glass=True):
    wins=(cx-11,cx+11)
    add_box(name+'_B',cx,y0+SILL/2,z,width,SILL,WALL,mat)
    top_h=FLOOR_H-SILL-WINDOW_H
    add_box(name+'_T',cx,y0+SILL+WINDOW_H+top_h/2,z,width,top_h,WALL,mat)
    segments=[(cx-width/2,wins[0]-WINDOW_W/2),(wins[0]+WINDOW_W/2,wins[1]-WINDOW_W/2),(wins[1]+WINDOW_W/2,cx+width/2)]
    for i,(a,b) in enumerate(segments): add_box(f'{name}_M{i}',(a+b)/2,y0+SILL+WINDOW_H/2,z,b-a,WINDOW_H,WALL,mat)
    if glass:
        for i,wx in enumerate(wins):
            add_box(f'{name}_Glass{i}',wx,y0+SILL+WINDOW_H/2,z,WINDOW_W-.35,WINDOW_H-.35,.16,'glass')
            # moldura, sem representar colisão futura
            add_box(f'{name}_FrameL{i}',wx-WINDOW_W/2,y0+SILL+WINDOW_H/2,z-.08,.18,WINDOW_H+.35,.22,'white')
            add_box(f'{name}_FrameR{i}',wx+WINDOW_W/2,y0+SILL+WINDOW_H/2,z-.08,.18,WINDOW_H+.35,.22,'white')
            add_box(f'{name}_FrameT{i}',wx,y0+SILL+WINDOW_H,z-.08,WINDOW_W+.35,.18,.22,'white')
            add_box(f'{name}_FrameB{i}',wx,y0+SILL,z-.08,WINDOW_W+.35,.18,.22,'white')

def add_core_front_wall(floor):
    y0=floor*FLOOR_H
    opening_w=12 if floor==0 else 9; opening_h=10 if floor==0 else 9
    side=(CORE_W-opening_w)/2
    add_box(f'CoreFront{floor}_L',-opening_w/2-side/2,y0+FLOOR_H/2,FRONT_Z,side,FLOOR_H,WALL,'white')
    add_box(f'CoreFront{floor}_R',opening_w/2+side/2,y0+FLOOR_H/2,FRONT_Z,side,FLOOR_H,WALL,'white')
    add_box(f'CoreFront{floor}_H',0,y0+opening_h+(FLOOR_H-opening_h)/2,FRONT_Z,opening_w,FLOOR_H-opening_h,WALL,'white')

def add_core_back_wall(floor):
    y0=floor*FLOOR_H
    # grande janela central da caixa da escada
    ww,wh=13,7; sill=3.5
    add_box(f'CoreBack{floor}_B',0,y0+sill/2,BACK_Z,CORE_W,sill,WALL,'white')
    th=FLOOR_H-sill-wh
    add_box(f'CoreBack{floor}_T',0,y0+sill+wh+th/2,BACK_Z,CORE_W,th,WALL,'white')
    side=(CORE_W-ww)/2
    add_box(f'CoreBack{floor}_L',-ww/2-side/2,y0+sill+wh/2,BACK_Z,side,wh,WALL,'white')
    add_box(f'CoreBack{floor}_R',ww/2+side/2,y0+sill+wh/2,BACK_Z,side,wh,WALL,'white')
    add_box(f'CoreBack{floor}_Glass',0,y0+sill+wh/2,BACK_Z-.08,ww-.3,wh-.3,.16,'glass')

def add_chair(name,x,y,z,rot=0):
    # rot 0/180 apenas, suficiente para referência de escala
    s=-1 if rot==180 else 1
    add_box(name+'_Seat',x,y+2.0,z,2.2,.25,2.1,'chair')
    add_box(name+'_Back',x,y+3.15,z+s*.95,2.2,2.15,.25,'chair')
    for dx in (-.82,.82):
        for dz in (-.72,.72): add_box(name+f'_Leg{dx}{dz}',x+dx,y+1,z+dz,.14,2,.14,'metal')
    add_box(name+'_ArmPost',x+1.18,y+2.65,z-s*.05,.15,1.6,.15,'metal')
    add_box(name+'_Tablet',x+1.25,y+3.42,z-s*.18,1.55,.12,1.9,'wood')

def add_r6(name,x,y,z):
    add_box(name+'_Torso',x,y+3,z,2,2,1,'uniform'); add_box(name+'_Head',x,y+4.5,z,2,1,1,'skin')
    add_box(name+'_ArmL',x-1.5,y+3,z,1,2,1,'skin'); add_box(name+'_ArmR',x+1.5,y+3,z,1,2,1,'skin')
    add_box(name+'_LegL',x-.5,y+1,z,1,2,1,'uniform'); add_box(name+'_LegR',x+.5,y+1,z,1,2,1,'uniform')

# --- pisos e teto ---
add_box('GroundSlab',0,-.25,0,BUILDING_W,.5,BUILDING_D,'concrete')
# laje do 2º andar com vazio real da escada no núcleo
add_box('Floor2_Left',-(CORE_W/2+(BUILDING_W/2-CORE_W/2)/2),FLOOR_H-.25,0,BUILDING_W/2-CORE_W/2,.5,BUILDING_D,'concrete')
add_box('Floor2_Right',+(CORE_W/2+(BUILDING_W/2-CORE_W/2)/2),FLOOR_H-.25,0,BUILDING_W/2-CORE_W/2,.5,BUILDING_D,'concrete')
add_box('Floor2_CoreFront',0,FLOOR_H-.25,-(18+(BUILDING_D/2-18)/2),CORE_W,.5,BUILDING_D/2-18,'concrete')
add_box('Floor2_CoreBack',0,FLOOR_H-.25,+(18+(BUILDING_D/2-18)/2),CORE_W,.5,BUILDING_D/2-18,'concrete')
add_box('Floor2_CoreSideL',-15.5,FLOOR_H-.25,0,5,.5,36,'concrete')
add_box('Floor2_CoreSideR',15.5,FLOOR_H-.25,0,5,.5,36,'concrete')
add_box('Ceiling2',0,BUILDING_H+.25,0,BUILDING_W,.5,BUILDING_D,'white')

# --- salas e corredor ---
for floor in range(2):
    y0=floor*FLOOR_H
    # paredes externas das 8 salas: 4 frente + 4 fundos
    for i,x in enumerate(ROOM_X):
        add_wall_two_windows(f'F{floor}_FrontRoom{i+1}',x,y0,FRONT_Z)
        add_wall_two_windows(f'F{floor}_BackRoom{i+5}',x,y0,BACK_Z)
        add_wall_with_door(f'F{floor}_FrontDoor{i+1}',x,y0,CORR_F,ROOM_W)
        add_wall_with_door(f'F{floor}_BackDoor{i+5}',x,y0,CORR_B,ROOM_W)
    # divisórias verticais das salas; o núcleo fica entre -18 e 18
    for x in (-106,-62,-18,18,62,106):
        add_box(f'F{floor}_DivFront{x}',x,y0+FLOOR_H/2,-29, WALL,FLOOR_H,40,'white')
        add_box(f'F{floor}_DivBack{x}',x,y0+FLOOR_H/2,29, WALL,FLOOR_H,40,'white')
    # paredes laterais externas
    add_box(f'F{floor}_SideL',-BUILDING_W/2,y0+FLOOR_H/2,0,WALL,FLOOR_H,BUILDING_D,'white')
    add_box(f'F{floor}_SideR', BUILDING_W/2,y0+FLOOR_H/2,0,WALL,FLOOR_H,BUILDING_D,'white')
    add_core_front_wall(floor); add_core_back_wall(floor)

# faixa verde baixa externa, visual
for z in (FRONT_Z-.55,BACK_Z+.55): add_box('GreenBand'+str(z),0,1,z,BUILDING_W,2,.12,'green')

# --- escada em U com patamar amplo ---
N=14; rise=7/N; tread=1.35
for i in range(N):
    top=(i+1)*rise
    z=-22 + i*tread
    add_box(f'StairA_{i+1}',-6,top-.25,z,8,.5,tread,'concrete')
# patamar intermediário
add_box('StairLanding',0,6.75,-2.0,20,.5,8,'concrete')
for i in range(N):
    top=7+(i+1)*rise
    z=1.0 - i*tread
    add_box(f'StairB_{i+1}',6,top-.25,z,8,.5,tread,'concrete')
# postes/corrimãos simples em arco visual no patamar
for j in range(9):
    a=math.pi*j/8
    x=9*math.cos(a); z=-2+4*math.sin(a)
    add_box(f'LandingRailPost{j}',x,8.6,z,.18,3.2,.18,'metal')
# corrimãos dos lances como barras segmentadas
for i in range(0,N,2):
    y=(i+1)*rise+1.9; z=-22+i*tread
    add_box(f'RailA{i}',-10,y,z,.18,.18,2.7,'metal')
    y2=7+(i+1)*rise+1.9; z2=1-i*tread
    add_box(f'RailB{i}',10,y2,z2,.18,.18,2.7,'metal')

# --- varanda central ---
BAL_W,BAL_D=40,13
add_box('BalconyFloor',0,FLOOR_H,FRONT_Z-BAL_D/2,BAL_W,.6,BAL_D,'concrete')
for x in (-16,16): add_box(f'BalconyPillar{x}',x,FLOOR_H/2,FRONT_Z-BAL_D+2,2.2,FLOOR_H,2.2,'white')
add_box('BalconyRailFront',0,FLOOR_H+2.0,FRONT_Z-BAL_D,BAL_W,3.5,.25,'metal')
add_box('BalconyRailL',-BAL_W/2,FLOOR_H+2.0,FRONT_Z-BAL_D/2,.25,3.5,BAL_D,'metal')
add_box('BalconyRailR', BAL_W/2,FLOOR_H+2.0,FRONT_Z-BAL_D/2,.25,3.5,BAL_D,'metal')
# barras verticais do guarda-corpo
for x in range(-18,19,3): add_box(f'BalRailPost{x}',x,FLOOR_H+2.0,FRONT_Z-BAL_D-.05,.16,3.5,.16,'metal')

# --- cobertura: águas inclinadas reais ---
EAVE=BUILDING_H+1.0; RIDGE=BUILDING_H+7.5
# alas esquerda/direita
for label,x0,x1 in [('Left',-108,-18),('Right',18,108)]:
    add_panel('Roof'+label+'Front',[(x0,EAVE,FRONT_Z-3),(x1,EAVE,FRONT_Z-3),(x1,RIDGE,0),(x0,RIDGE,0)],.4,'roof')
    add_panel('Roof'+label+'Back',[(x0,RIDGE,0),(x1,RIDGE,0),(x1,EAVE,BACK_Z+3),(x0,EAVE,BACK_Z+3)],.4,'roof')
# pavilhão central elevado, mantendo o meio destacado
CE=BUILDING_H+4.0; CR=BUILDING_H+10.0
add_box('CentralPavilionL',-CORE_W/2,BUILDING_H+3.0,0,1,6,76,'white')
add_box('CentralPavilionR', CORE_W/2,BUILDING_H+3.0,0,1,6,76,'white')
add_box('CentralPavilionFront',0,BUILDING_H+3.0,-38,CORE_W,6,1,'white')
add_box('CentralPavilionBack',0,BUILDING_H+3.0,38,CORE_W,6,1,'white')
add_panel('RoofCoreFront',[(-22,CE,-41),(22,CE,-41),(22,CR,0),(-22,CR,0)],.4,'roof')
add_panel('RoofCoreBack',[(-22,CR,0),(22,CR,0),(22,CE,41),(-22,CE,41)],.4,'roof')
# cobertura da varanda como pequena água inclinada
add_panel('BalconyCanopy',[(-22,EAVE+2,FRONT_Z-1),(22,EAVE+2,FRONT_Z-1),(22,EAVE+.8,FRONT_Z-BAL_D-2),(-22,EAVE+.8,FRONT_Z-BAL_D-2)],.3,'roof')

# --- cadeiras: 1 por sala, 8 por andar ---
for floor in range(2):
    y=floor*FLOOR_H
    for i,x in enumerate(ROOM_X):
        add_chair(f'Chair_F{floor}_R{i+1}',x,y,-29,180)
        add_chair(f'Chair_F{floor}_R{i+5}',x,y,29,0)
add_r6('R6_Ground',-8,0,-4); add_r6('R6_Second',8,FLOOR_H,-28)

# --- OBJ / MTL ---
mtl=[]
for name,hexv in MAT.items():
    r=int(hexv[0:2],16)/255; g=int(hexv[2:4],16)/255; b=int(hexv[4:6],16)/255
    alpha=.45 if name=='glass' else 1
    mtl += [f'newmtl {name}',f'Kd {r:.4f} {g:.4f} {b:.4f}',f'd {alpha}','illum 2','']
(OUT/'bloco_instrucao.mtl').write_text('\n'.join(mtl),encoding='utf-8')
obj=['mtllib bloco_instrucao.mtl']+[f'v {x:.5f} {y:.5f} {z:.5f}' for x,y,z in verts]
last=None
for ids,mat,grp in faces:
    if grp!=last: obj.append(f'g {grp}'); last=grp
    obj.append(f'usemtl {mat}'); obj.append('f '+' '.join(map(str,ids)))
(OUT/'bloco_instrucao.obj').write_text('\n'.join(obj),encoding='utf-8')

# --- previews SVG sem dependências externas ---
COLOR={k:'#'+v for k,v in MAT.items()}
def svg_iso(path, az=-45, el=26, title='Bloco de Instrução 3D'):
    az=math.radians(az); el=math.radians(el)
    def proj(p):
        x,y,z=p; xa=x*math.cos(az)-z*math.sin(az); za=x*math.sin(az)+z*math.cos(az)
        yy=y*math.cos(el)-za*math.sin(el); depth=y*math.sin(el)+za*math.cos(el)
        return xa,-yy,depth
    pf=[]
    for ids,mat,grp in faces:
        pts=[verts[i-1] for i in ids]; pp=[proj(p) for p in pts]; pf.append((sum(q[2] for q in pp)/len(pp),[(q[0],q[1]) for q in pp],mat))
    allp=[q for _,poly,_ in pf for q in poly]; minx=min(x for x,y in allp); maxx=max(x for x,y in allp); miny=min(y for x,y in allp); maxy=max(y for x,y in allp)
    W,H=1400,850; s=min((W-80)/(maxx-minx),(H-100)/(maxy-miny)); ox=40-minx*s; oy=70-miny*s
    lines=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}"><rect width="100%" height="100%" fill="#e8eef2"/><text x="40" y="38" font-family="Arial" font-size="24" font-weight="700">{title}</text>']
    for _,poly,mat in sorted(pf,key=lambda a:a[0]):
        pts=' '.join(f'{ox+x*s:.1f},{oy+y*s:.1f}' for x,y in poly)
        opacity='.55' if mat=='glass' else '1'
        lines.append(f'<polygon points="{pts}" fill="{COLOR[mat]}" fill-opacity="{opacity}" stroke="#343434" stroke-width="0.45"/>')
    lines.append('</svg>'); path.write_text('\n'.join(lines),encoding='utf-8')

def svg_plan(path,floor=0):
    y0=floor*FLOOR_H; W,H=1200,650; sx=(W-80)/BUILDING_W; sz=(H-100)/BUILDING_D
    X=lambda x:40+(x+BUILDING_W/2)*sx; Z=lambda z:60+(z+BUILDING_D/2)*sz
    s=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}"><rect width="100%" height="100%" fill="white"/><text x="40" y="35" font-family="Arial" font-size="24" font-weight="700">PLANTA — {"TÉRREO" if floor==0 else "2º ANDAR"}</text>',f'<rect x="{X(-BUILDING_W/2)}" y="{Z(-BUILDING_D/2)}" width="{BUILDING_W*sx}" height="{BUILDING_D*sz}" fill="#f8f8f4" stroke="#222" stroke-width="3"/>']
    for x in (-62,-18,18,62): s.append(f'<line x1="{X(x)}" y1="{Z(-BUILDING_D/2)}" x2="{X(x)}" y2="{Z(-CORRIDOR_W/2)}" stroke="#333" stroke-width="2"/><line x1="{X(x)}" y1="{Z(CORRIDOR_W/2)}" x2="{X(x)}" y2="{Z(BUILDING_D/2)}" stroke="#333" stroke-width="2"/>')
    s.append(f'<line x1="{X(-BUILDING_W/2)}" y1="{Z(CORR_F)}" x2="{X(-CORE_W/2)}" y2="{Z(CORR_F)}" stroke="#333" stroke-width="2"/><line x1="{X(CORE_W/2)}" y1="{Z(CORR_F)}" x2="{X(BUILDING_W/2)}" y2="{Z(CORR_F)}" stroke="#333" stroke-width="2"/>')
    s.append(f'<line x1="{X(-BUILDING_W/2)}" y1="{Z(CORR_B)}" x2="{X(-CORE_W/2)}" y2="{Z(CORR_B)}" stroke="#333" stroke-width="2"/><line x1="{X(CORE_W/2)}" y1="{Z(CORR_B)}" x2="{X(BUILDING_W/2)}" y2="{Z(CORR_B)}" stroke="#333" stroke-width="2"/>')
    s.append(f'<rect x="{X(-CORE_W/2)}" y="{Z(-18)}" width="{CORE_W*sx}" height="{36*sz}" fill="#e6e6dc" stroke="#444"/><text x="{X(-12)}" y="{Z(1)}" font-family="Arial" font-size="16">HALL + ESCADA</text>')
    for i,x in enumerate(ROOM_X):
        for z,idx in [(-29,i+1),(29,i+5)]: s.append(f'<text x="{X(x)-18}" y="{Z(z)}" font-family="Arial" font-size="15">SALA {idx}</text>')
    if floor==1: s.append(f'<rect x="{X(-20)}" y="{Z(FRONT_Z-BAL_D)}" width="{40*sx}" height="{BAL_D*sz}" fill="#ddd" stroke="#333"/><text x="{X(-9)}" y="{Z(FRONT_Z-5)}" font-family="Arial" font-size="14">VARANDA</text>')
    s.append('</svg>'); path.write_text('\n'.join(s),encoding='utf-8')

svg_iso(OUT/'preview_iso_frente.svg',-45,25,'BLOCO DE INSTRUÇÃO — VISTA 3D FRONTAL')
svg_iso(OUT/'preview_iso_traseira.svg',135,25,'BLOCO DE INSTRUÇÃO — VISTA 3D TRASEIRA')
svg_plan(OUT/'planta_terreo.svg',0); svg_plan(OUT/'planta_2_andar.svg',1)

summary={
 'dimensions_studs':{'width':BUILDING_W,'depth':BUILDING_D,'floor_height':FLOOR_H,'total_wall_height':BUILDING_H},
 'rooms_per_floor':8,'total_rooms':16,'room_size':[ROOM_W,ROOM_D],'corridor_width':CORRIDOR_W,
 'central_core_width':CORE_W,'balcony':{'width':BAL_W,'depth':BAL_D,'floor':2},
 'stairs':{'type':'U','steps_per_flight':N,'intermediate_landing':True},
 'roof':{'type':'pitched_gable','real_sloped_geometry':True,'central_raised_pavilion':True},
 'chairs':16,'r6_references':2,'vertices':len(verts),'faces':len(faces)
}
(OUT/'model_summary.json').write_text(json.dumps(summary,indent=2,ensure_ascii=False),encoding='utf-8')

with zipfile.ZipFile(OUT/'Bloco_Instrucao_3D.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in OUT.iterdir():
        if p.name!='Bloco_Instrucao_3D.zip': z.write(p,p.name)
print(json.dumps(summary,ensure_ascii=False))
