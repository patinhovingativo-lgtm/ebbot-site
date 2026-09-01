import os, runpy, math

# Build V7 first, then replace every corridor-facing classroom wall with a new
# wall that has TWO separate openings:
#   1) open doorway near the classroom corner (NO DOOR LEAF)
#   2) large observation window beside it, facing the internal corridor
# This lets someone in the corridor see inside the classroom without entering.
runpy.run_path('tools/gen_bloco_instrucao_v7.py', run_name='__main__')

src='models/v7/BlocoInstrucao_V7.obj'
src_mtl='models/v7/BlocoInstrucao_V7.mtl'
out='models/v8'
os.makedirs(out, exist_ok=True)

with open(src,'r',encoding='utf-8') as f:
    lines=f.readlines()

# Remove the COMPLETE old classroom corridor walls from V7.
# These objects are all named FrontDoor*/BackDoor* in the V6/V7 source,
# including their wall pieces. We rebuild them correctly below.
filtered=[]
skip=False
for line in lines:
    if line.startswith('o '):
        name=line[2:].strip()
        skip=('FrontDoor' in name or 'BackDoor' in name)
    if not skip:
        filtered.append(line)

for i,line in enumerate(filtered):
    if line.startswith('mtllib '):
        filtered[i]='mtllib BlocoInstrucao_V8.mtl\n'
        break

vertex_count=sum(1 for l in filtered if l.startswith('v '))
append=[]

def addv(p):
    global vertex_count
    vertex_count+=1
    append.append('v %.5f %.5f %.5f\n' % p)
    return vertex_count

def add_obj(name,mat):
    append.append(f'o {name}\n')
    append.append(f'usemtl {mat}\n')

def box(name,cx,cy,cz,sx,sy,sz,mat='WhiteBrick'):
    add_obj(name,mat)
    hx,hy,hz=sx/2,sy/2,sz/2
    pts=[(-hx,-hy,-hz),(hx,-hy,-hz),(hx,hy,-hz),(-hx,hy,-hz),
         (-hx,-hy,hz),(hx,-hy,hz),(hx,hy,hz),(-hx,hy,hz)]
    ids=[addv((cx+x,cy+y,cz+z)) for x,y,z in pts]
    for a,b,c,d in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]:
        append.append(f'f {ids[a]} {ids[b]} {ids[c]} {ids[d]}\n')

def corridor_classroom_wall(prefix,xa,xb,y0,z,door_side='left',room_side='front'):
    """Classroom wall facing the corridor.

    Door opening sits near one corner and remains fully OPEN.
    A large glazed observation window sits immediately beside the doorway,
    in the SAME wall, so corridor users can see into the classroom.
    """
    H=14.0
    dw,dh=5.8,8.8
    margin=2.0
    gap=2.0
    ww,wh=10.5,5.2
    sill=3.6

    if door_side=='left':
        dl=xa+margin
        dr=dl+dw
        wl=dr+gap
        wr=wl+ww
    else:
        dr=xb-margin
        dl=dr-dw
        wr=dl-gap
        wl=wr-ww

    # Clamp observation window if a narrower room is ever used.
    if wl < xa+0.8:
        shift=(xa+0.8)-wl; wl+=shift; wr+=shift
    if wr > xb-0.8:
        shift=wr-(xb-0.8); wl-=shift; wr-=shift

    # Full-height solid wall pieces outside the door/window system.
    points=sorted([xa,dl,dr,wl,wr,xb])
    # Solid full-height intervals that are neither door nor window.
    intervals=[]
    for a,b in zip(points[:-1],points[1:]):
        mid=(a+b)/2
        inside_door=(dl <= mid <= dr)
        inside_win=(wl <= mid <= wr)
        if not inside_door and not inside_win and b-a>1e-4:
            intervals.append((a,b))
    for i,(a,b) in enumerate(intervals):
        box(prefix+f'_Solid{i}',(a+b)/2,y0+H/2,z,b-a,H,1,'WhiteBrick')

    # Door: OPEN from floor to dh, only lintel/header above it.
    box(prefix+'_DoorHeader',(dl+dr)/2,y0+dh+(H-dh)/2,z,dw,H-dh,1,'WhiteBrick')

    # Small architectural jamb trim around open doorway (no leaf, no glass).
    trimz = z + (0.18 if room_side=='front' else -0.18)
    box(prefix+'_DoorTrimL',dl,y0+dh/2,trimz,0.22,dh,0.30,'ConcreteLight')
    box(prefix+'_DoorTrimR',dr,y0+dh/2,trimz,0.22,dh,0.30,'ConcreteLight')
    box(prefix+'_DoorTrimTop',(dl+dr)/2,y0+dh,trimz,dw+0.22,0.22,0.30,'ConcreteLight')

    # Observation window opening: wall below + wall above.
    box(prefix+'_ObsBottom',(wl+wr)/2,y0+sill/2,z,ww,sill,1,'WhiteBrick')
    top_h=H-(sill+wh)
    box(prefix+'_ObsTop',(wl+wr)/2,y0+sill+wh+top_h/2,z,ww,top_h,1,'WhiteBrick')

    # Window glass is centered through wall thickness, visible from BOTH corridor
    # and classroom. The frame is double-sided so it does not look painted on.
    box(prefix+'_ObsGlass',(wl+wr)/2,y0+sill+wh/2,z,ww-0.48,wh-0.42,0.18,'Glass')
    for x in (wl,wr):
        box(prefix+('_ObsFrameL' if x==wl else '_ObsFrameR'),x,y0+sill+wh/2,z,0.28,wh+0.55,0.36,'MetalDark')
    box(prefix+'_ObsFrameTop',(wl+wr)/2,y0+sill+wh,z,ww+0.45,0.28,0.36,'MetalDark')
    box(prefix+'_ObsFrameBottom',(wl+wr)/2,y0+sill,z,ww+0.45,0.28,0.36,'MetalDark')
    box(prefix+'_ObsMullion',(wl+wr)/2,y0+sill+wh/2,z,0.16,wh-0.25,0.30,'MetalDark')

    # Interior/corridor sill projection is shallow, not climbable-looking.
    toward_corr = 1 if room_side=='front' else -1
    box(prefix+'_ObsSill',(wl+wr)/2,y0+sill-0.18,z+toward_corr*0.40,ww+0.7,0.22,0.72,'ConcreteLight')

# Same geometry/ranges as V7/V6.
FH=14.0
cf=-10.0
cb=10.0
front_ranges=[(-92,-54),(-54,-16),(16,54),(54,92)]
back_ranges=[(-92,-46),(-46,0),(0,46),(46,92)]
front_sides=['left','right','left','right']
back_sides=['left','left','right','right']

for floor in (0,1):
    y0=floor*FH
    for i,(xa,xb) in enumerate(front_ranges):
        corridor_classroom_wall(f'A{floor+1}_FrontClassroomCorridor{i+1}',xa,xb,y0,cf,front_sides[i],'front')
    for i,(xa,xb) in enumerate(back_ranges):
        corridor_classroom_wall(f'A{floor+1}_BackClassroomCorridor{i+1}',xa,xb,y0,cb,back_sides[i],'back')

# Add a pair of side windows to the projecting central upper pavilion, making
# the mass less blank while keeping the main classroom observation windows inside.
# These are visual side windows on the central bay, not extra classroom doors.
for side,x in [('L',-15.48),('R',15.48)]:
    # x wall is about +/-16; glass projects slightly inward/outward around it.
    for floor,yc in [(1,20.8)]:
        box(f'CentralSideWindow_{side}_Glass',x,yc,-36.5,0.18,4.8,8.8,'Glass')
        box(f'CentralSideWindow_{side}_FrameTop',x,yc+2.5,-36.5,0.34,0.24,9.3,'MetalDark')
        box(f'CentralSideWindow_{side}_FrameBottom',x,yc-2.5,-36.5,0.34,0.24,9.3,'MetalDark')
        box(f'CentralSideWindow_{side}_FrameFront',x,yc,-40.9,0.34,5.2,0.24,'MetalDark')
        box(f'CentralSideWindow_{side}_FrameBack',x,yc,-32.1,0.34,5.2,0.24,'MetalDark')
        box(f'CentralSideWindow_{side}_Mullion',x,yc,-36.5,0.30,4.6,0.16,'MetalDark')

filtered.insert(1,'# V8: open classroom entries near corners + corridor observation windows into every classroom\n')
with open(os.path.join(out,'BlocoInstrucao_V8.obj'),'w',encoding='utf-8') as f:
    f.writelines(filtered)
    f.write('\n# --- V8 rebuilt classroom corridor walls ---\n')
    f.writelines(append)

with open(src_mtl,'r',encoding='utf-8') as f:
    mtl=f.read()
with open(os.path.join(out,'BlocoInstrucao_V8.mtl'),'w',encoding='utf-8') as f:
    f.write(mtl)

readme='''BLOCO DE INSTRUCAO V8\n\nCORRECOES:\n- Nenhuma folha de porta nas salas. As entradas continuam como VAOS abertos perto do canto.\n- Cada sala agora tem uma JANELA DE OBSERVACAO voltada para o corredor, ao lado do vao.\n- A janela atravessa a parede e pode ser vista dos dois lados, permitindo olhar para dentro da sala sem entrar.\n- Mantidas as duas janelas externas de cada sala para iluminacao natural.\n- Adicionadas janelas laterais discretas no pavilhao central superior para reduzir paredes cegas.\n- Empenas, telhados, varanda, corredor e demais detalhes do V7 foram preservados.\n'''
with open(os.path.join(out,'README_V8.txt'),'w',encoding='utf-8') as f:
    f.write(readme)

print('V8 generated with corridor observation windows')
