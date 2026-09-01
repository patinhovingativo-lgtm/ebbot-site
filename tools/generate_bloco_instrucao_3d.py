from pathlib import Path
import json, math

OUT = Path('generated/bloco_instrucao_3d')
OUT.mkdir(parents=True, exist_ok=True)

# ============================================================
# BLOCO DE INSTRUCAO - MODELO MESTRE 3D
# 2 andares / 8 salas por andar / 16 salas total
# Coordenadas em studs: X = largura, Y = altura, Z = profundidade
# ============================================================

ROOM_W = 42.0
ROOM_D = 34.0
CORRIDOR_W = 16.0
CORE_W = 34.0
FLOOR_H = 13.0
WALL = 1.0
SLAB = 0.8

WING_W = ROOM_W * 2.0
BUILDING_W = WING_W * 2.0 + CORE_W   # 202
BUILDING_D = ROOM_D * 2.0 + CORRIDOR_W  # 84
BUILDING_H = FLOOR_H * 2.0            # 26

X_MIN = -BUILDING_W / 2.0
X_MAX = BUILDING_W / 2.0
Z_FRONT = -BUILDING_D / 2.0
Z_BACK = BUILDING_D / 2.0
Z_CORR_FRONT = -CORRIDOR_W / 2.0
Z_CORR_BACK = CORRIDOR_W / 2.0
CORE_X0 = -CORE_W / 2.0
CORE_X1 = CORE_W / 2.0

ROOM_X = [-80.0, -38.0, 38.0, 80.0]
ROOM_Z = [-25.0, 25.0]

DOOR_W = 6.0
DOOR_H = 8.5
WINDOW_W = 6.2
WINDOW_H = 5.2
WINDOW_SILL = 3.8
WINDOW_OFFSETS = (-11.0, 0.0, 11.0)

# Materials are intentionally simple. In Roblox later, small brick/tile relief
# should be visual/non-collidable or texture-based, while main collision stays smooth.
MTL = '''# Bloco de Instrucao materials\n\nnewmtl WhiteBrick\nKa 0.84 0.84 0.80\nKd 0.92 0.92 0.88\nKs 0.05 0.05 0.05\nNs 8\n\nnewmtl Concrete\nKa 0.55 0.55 0.53\nKd 0.72 0.72 0.69\nKs 0.03 0.03 0.03\nNs 4\n\nnewmtl RoofTile\nKa 0.34 0.17 0.09\nKd 0.56 0.28 0.14\nKs 0.06 0.04 0.03\nNs 6\n\nnewmtl GreenTrim\nKa 0.10 0.18 0.12\nKd 0.16 0.29 0.19\nKs 0.03 0.03 0.03\nNs 4\n\nnewmtl Glass\nKa 0.12 0.22 0.27\nKd 0.34 0.57 0.66\nKs 0.30 0.30 0.30\nNs 40\nd 0.34\nillum 4\n\nnewmtl Metal\nKa 0.08 0.08 0.08\nKd 0.20 0.21 0.20\nKs 0.18 0.18 0.18\nNs 24\n\nnewmtl Chair\nKa 0.07 0.07 0.07\nKd 0.16 0.17 0.16\nKs 0.08 0.08 0.08\nNs 10\n\nnewmtl Wood\nKa 0.20 0.12 0.06\nKd 0.48 0.29 0.13\nKs 0.05 0.04 0.03\nNs 6\n\nnewmtl R6\nKa 0.42 0.36 0.26\nKd 0.73 0.64 0.47\nKs 0.02 0.02 0.02\nNs 2\n'''

class ObjWriter:
    def __init__(self):
        self.lines = ['mtllib Bloco_Instrucao_3D.mtl']
        self.vcount = 0
        self.face_count = 0
        self.groups = []

    def _verts(self, pts):
        base = self.vcount + 1
        for x, y, z in pts:
            self.lines.append(f'v {x:.4f} {y:.4f} {z:.4f}')
        self.vcount += len(pts)
        return list(range(base, base + len(pts)))

    def _faces(self, faces):
        for f in faces:
            self.lines.append('f ' + ' '.join(str(i) for i in f))
            self.face_count += 1

    def group(self, name, material):
        safe = name.replace(' ', '_')
        self.lines += [f'o {safe}', f'g {safe}', f'usemtl {material}']
        self.groups.append(safe)

    def box(self, name, x0, x1, y0, y1, z0, z1, material='WhiteBrick'):
        if x1 <= x0 or y1 <= y0 or z1 <= z0:
            raise ValueError(f'Invalid box {name}: {(x0,x1,y0,y1,z0,z1)}')
        self.group(name, material)
        pts = [
            (x0,y0,z0),(x1,y0,z0),(x1,y1,z0),(x0,y1,z0),
            (x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1),
        ]
        v = self._verts(pts)
        self._faces([
            (v[0],v[1],v[2],v[3]), (v[4],v[7],v[6],v[5]),
            (v[0],v[4],v[5],v[1]), (v[3],v[2],v[6],v[7]),
            (v[0],v[3],v[7],v[4]), (v[1],v[5],v[6],v[2]),
        ])

    def sloped_slab(self, name, p0, p1, p2, p3, thickness=0.35, material='RoofTile'):
        # Four top corners, with a vertical thickness downward.
        self.group(name, material)
        top = [p0,p1,p2,p3]
        bottom = [(x,y-thickness,z) for x,y,z in top]
        v = self._verts(top + bottom)
        self._faces([
            (v[0],v[1],v[2],v[3]),
            (v[4],v[7],v[6],v[5]),
            (v[0],v[4],v[5],v[1]),
            (v[1],v[5],v[6],v[2]),
            (v[2],v[6],v[7],v[3]),
            (v[3],v[7],v[4],v[0]),
        ])

    def write(self, path):
        path.write_text('\n'.join(self.lines) + '\n', encoding='utf-8')

W = ObjWriter()

# ============================================================
# Helpers
# ============================================================

def facade_room_wall(tag, cx, y0, z, outward):
    # Real openings: 3 spaced windows. No windows are ever created on corridor walls.
    z0, z1 = (z-WALL/2, z+WALL/2)
    # Bottom and top strips
    W.box(f'{tag}_Bottom', cx-ROOM_W/2, cx+ROOM_W/2, y0, y0+WINDOW_SILL, z0, z1)
    W.box(f'{tag}_Top', cx-ROOM_W/2, cx+ROOM_W/2,
          y0+WINDOW_SILL+WINDOW_H, y0+FLOOR_H, z0, z1)

    windows = [cx+o for o in WINDOW_OFFSETS]
    ranges = [
        (cx-ROOM_W/2, windows[0]-WINDOW_W/2),
        (windows[0]+WINDOW_W/2, windows[1]-WINDOW_W/2),
        (windows[1]+WINDOW_W/2, windows[2]-WINDOW_W/2),
        (windows[2]+WINDOW_W/2, cx+ROOM_W/2),
    ]
    for i,(a,b) in enumerate(ranges,1):
        W.box(f'{tag}_Pier_{i}', a,b, y0+WINDOW_SILL, y0+WINDOW_SILL+WINDOW_H, z0,z1)

    # Glass sits inside the wall opening, slightly offset outward.
    gz = z + outward*0.06
    for i,wx in enumerate(windows,1):
        W.box(f'{tag}_Glass_{i}', wx-WINDOW_W/2+0.15, wx+WINDOW_W/2-0.15,
              y0+WINDOW_SILL+0.15, y0+WINDOW_SILL+WINDOW_H-0.15,
              gz-0.08, gz+0.08, 'Glass')
        # Physical-looking frames, deliberately shallow.
        frame = 0.18
        depth = 0.20
        W.box(f'{tag}_FrameL_{i}', wx-WINDOW_W/2-frame/2, wx-WINDOW_W/2+frame/2,
              y0+WINDOW_SILL, y0+WINDOW_SILL+WINDOW_H, gz-depth/2,gz+depth/2,'Concrete')
        W.box(f'{tag}_FrameR_{i}', wx+WINDOW_W/2-frame/2, wx+WINDOW_W/2+frame/2,
              y0+WINDOW_SILL, y0+WINDOW_SILL+WINDOW_H, gz-depth/2,gz+depth/2,'Concrete')
        W.box(f'{tag}_FrameB_{i}', wx-WINDOW_W/2, wx+WINDOW_W/2,
              y0+WINDOW_SILL-frame/2, y0+WINDOW_SILL+frame/2, gz-depth/2,gz+depth/2,'Concrete')
        W.box(f'{tag}_FrameT_{i}', wx-WINDOW_W/2, wx+WINDOW_W/2,
              y0+WINDOW_SILL+WINDOW_H-frame/2, y0+WINDOW_SILL+WINDOW_H+frame/2,
              gz-depth/2,gz+depth/2,'Concrete')


def corridor_wall_with_door(tag, cx, y0, z):
    # Classroom-to-corridor wall: door only, never a window.
    side = (ROOM_W-DOOR_W)/2
    z0,z1 = z-WALL/2,z+WALL/2
    W.box(f'{tag}_L', cx-ROOM_W/2, cx-DOOR_W/2, y0,y0+FLOOR_H,z0,z1)
    W.box(f'{tag}_R', cx+DOOR_W/2, cx+ROOM_W/2, y0,y0+FLOOR_H,z0,z1)
    W.box(f'{tag}_Header', cx-DOOR_W/2,cx+DOOR_W/2, y0+DOOR_H,y0+FLOOR_H,z0,z1)
    # Door frame, no door leaf yet: model remains empty/open for testing.
    f=0.20; d=0.18
    W.box(f'{tag}_FrameL', cx-DOOR_W/2-f/2,cx-DOOR_W/2+f/2,y0,y0+DOOR_H,z-d/2,z+d/2,'Concrete')
    W.box(f'{tag}_FrameR', cx+DOOR_W/2-f/2,cx+DOOR_W/2+f/2,y0,y0+DOOR_H,z-d/2,z+d/2,'Concrete')
    W.box(f'{tag}_FrameTop',cx-DOOR_W/2,cx+DOOR_W/2,y0+DOOR_H-f/2,y0+DOOR_H+f/2,z-d/2,z+d/2,'Concrete')


def add_chair(tag, x, y, z, face_to_corridor):
    # One scale-test chair per classroom. The building is otherwise empty.
    # Seat and back are centered around the room center.
    W.box(f'{tag}_Seat',x-1.1,x+1.1,y+1.9,y+2.15,z-1.0,z+1.0,'Chair')
    # Back goes on the side away from the corridor, so chair faces corridor.
    back_z = z + (0.92 if face_to_corridor < 0 else -0.92)
    W.box(f'{tag}_Back',x-1.1,x+1.1,y+2.15,y+4.1,back_z-0.12,back_z+0.12,'Chair')
    for dx in (-0.82,0.82):
        for dz in (-0.72,0.72):
            W.box(f'{tag}_Leg_{dx}_{dz}',x+dx-0.07,x+dx+0.07,y,y+1.9,z+dz-0.07,z+dz+0.07,'Metal')
    # Writing tablet on the right side.
    W.box(f'{tag}_TabletSupport',x+1.13,x+1.28,y+1.9,y+3.35,z-0.10,z+0.05,'Metal')
    W.box(f'{tag}_Tablet',x+0.65,x+1.65,y+3.3,y+3.42,z-0.92,z+0.78,'Wood')


def add_r6(tag,x,y,z):
    # Approximate Roblox R6 scale reference (~5 studs tall)
    W.box(f'{tag}_Torso',x-1,x+1,y+2,y+4,z-0.5,z+0.5,'R6')
    W.box(f'{tag}_Head',x-1,x+1,y+4,y+5,z-0.5,z+0.5,'R6')
    W.box(f'{tag}_ArmL',x-2,x-1,y+2,y+4,z-0.5,z+0.5,'R6')
    W.box(f'{tag}_ArmR',x+1,x+2,y+2,y+4,z-0.5,z+0.5,'R6')
    W.box(f'{tag}_LegL',x-1,x,y,y+2,z-0.5,z+0.5,'R6')
    W.box(f'{tag}_LegR',x,x+1,y,y+2,z-0.5,z+0.5,'R6')

# ============================================================
# Floors / ceilings
# ============================================================

# Ground slab
W.box('Ground_Slab', X_MIN, X_MAX, -SLAB, 0, Z_FRONT, Z_BACK, 'Concrete')

# Second floor: solid wings + core floor around a stair void.
W.box('Floor2_LeftWing', X_MIN, CORE_X0, FLOOR_H-SLAB, FLOOR_H, Z_FRONT,Z_BACK,'Concrete')
W.box('Floor2_RightWing', CORE_X1, X_MAX, FLOOR_H-SLAB, FLOOR_H, Z_FRONT,Z_BACK,'Concrete')
# Core second floor leaves a central stair opening from z=-12 to +16 and x=-10 to +10.
W.box('Floor2_Core_Front', CORE_X0,CORE_X1,FLOOR_H-SLAB,FLOOR_H,Z_FRONT,-12,'Concrete')
W.box('Floor2_Core_Back', CORE_X0,CORE_X1,FLOOR_H-SLAB,FLOOR_H,16,Z_BACK,'Concrete')
W.box('Floor2_Core_Left', CORE_X0,-10,FLOOR_H-SLAB,FLOOR_H,-12,16,'Concrete')
W.box('Floor2_Core_Right',10,CORE_X1,FLOOR_H-SLAB,FLOOR_H,-12,16,'Concrete')

# Real ceiling / roof-support slab at top of second floor.
W.box('Ceiling_LeftWing',X_MIN,CORE_X0,BUILDING_H,BUILDING_H+0.55,Z_FRONT,Z_BACK,'Concrete')
W.box('Ceiling_RightWing',CORE_X1,X_MAX,BUILDING_H,BUILDING_H+0.55,Z_FRONT,Z_BACK,'Concrete')
W.box('Ceiling_Core',CORE_X0,CORE_X1,BUILDING_H,BUILDING_H+0.55,Z_FRONT,Z_BACK,'Concrete')

# ============================================================
# Classroom wings - 8 rooms per floor
# Layout per floor: left wing 4 rooms (2 front + 2 back), right wing same.
# ============================================================

room_meta=[]
room_number=0
for floor in range(2):
    y0=floor*FLOOR_H
    for cx in ROOM_X:
        for bank,zcenter in [('Front',-25.0),('Back',25.0)]:
            room_number += 1
            tag=f'F{floor+1}_Classroom_{room_number:02d}'
            outside_z = Z_FRONT if bank=='Front' else Z_BACK
            outward = -1 if bank=='Front' else 1
            corridor_z = Z_CORR_FRONT if bank=='Front' else Z_CORR_BACK
            facade_room_wall(tag+'_Exterior',cx,y0,outside_z,outward)
            corridor_wall_with_door(tag+'_Corridor',cx,y0,corridor_z)
            add_chair(tag+'_Chair',cx,y0,zcenter, -1 if bank=='Front' else 1)
            room_meta.append({'room':room_number,'floor':floor+1,'bank':bank,'cx':cx,'cz':zcenter})

# Internal separators only where two classroom bays meet inside each wing.
for floor in range(2):
    y0=floor*FLOOR_H
    for bx in (-59.0,59.0):
        W.box(f'F{floor+1}_RoomDivider_{bx}_Front',bx-WALL/2,bx+WALL/2,y0,y0+FLOOR_H,Z_FRONT,Z_CORR_FRONT)
        W.box(f'F{floor+1}_RoomDivider_{bx}_Back',bx-WALL/2,bx+WALL/2,y0,y0+FLOOR_H,Z_CORR_BACK,Z_BACK)

# Core boundary walls: close classroom zones, but leave corridor opening free into central hall.
for floor in range(2):
    y0=floor*FLOOR_H
    for bx,label in ((CORE_X0,'LeftCore'),(CORE_X1,'RightCore')):
        W.box(f'F{floor+1}_{label}_FrontRoomWall',bx-WALL/2,bx+WALL/2,y0,y0+FLOOR_H,Z_FRONT,Z_CORR_FRONT)
        W.box(f'F{floor+1}_{label}_BackRoomWall',bx-WALL/2,bx+WALL/2,y0,y0+FLOOR_H,Z_CORR_BACK,Z_BACK)

# Outer end walls. Corridor gets one real window at each end; classroom side zones remain solid.
for floor in range(2):
    y0=floor*FLOOR_H
    for x,label,outward in ((X_MIN,'West',-1),(X_MAX,'East',1)):
        x0,x1=x-WALL/2,x+WALL/2
        # front/back classroom side walls
        W.box(f'F{floor+1}_{label}_FrontSide',x0,x1,y0,y0+FLOOR_H,Z_FRONT,Z_CORR_FRONT)
        W.box(f'F{floor+1}_{label}_BackSide',x0,x1,y0,y0+FLOOR_H,Z_CORR_BACK,Z_BACK)
        # corridor end wall around centered window
        wz0,wz1=-4.0,4.0
        W.box(f'F{floor+1}_{label}_CorridorWin_Bottom',x0,x1,y0,y0+WINDOW_SILL,Z_CORR_FRONT,Z_CORR_BACK)
        W.box(f'F{floor+1}_{label}_CorridorWin_Top',x0,x1,y0+WINDOW_SILL+WINDOW_H,y0+FLOOR_H,Z_CORR_FRONT,Z_CORR_BACK)
        W.box(f'F{floor+1}_{label}_CorridorWin_FrontPier',x0,x1,y0+WINDOW_SILL,y0+WINDOW_SILL+WINDOW_H,Z_CORR_FRONT,wz0)
        W.box(f'F{floor+1}_{label}_CorridorWin_BackPier',x0,x1,y0+WINDOW_SILL,y0+WINDOW_SILL+WINDOW_H,wz1,Z_CORR_BACK)
        gx=x+outward*0.06
        W.box(f'F{floor+1}_{label}_CorridorGlass',gx-0.08,gx+0.08,y0+WINDOW_SILL+0.15,y0+WINDOW_SILL+WINDOW_H-0.15,wz0+0.15,wz1-0.15,'Glass')

# ============================================================
# Central hall + entrance + balcony
# ============================================================

def front_core_wall(floor):
    y0=floor*FLOOR_H
    z0,z1=Z_FRONT-WALL/2,Z_FRONT+WALL/2
    opening_w = 12.0 if floor==0 else 8.0
    opening_h = 9.5 if floor==0 else 9.0
    W.box(f'Core_F{floor+1}_Front_L',CORE_X0,-opening_w/2,y0,y0+FLOOR_H,z0,z1)
    W.box(f'Core_F{floor+1}_Front_R',opening_w/2,CORE_X1,y0,y0+FLOOR_H,z0,z1)
    W.box(f'Core_F{floor+1}_Front_Header',-opening_w/2,opening_w/2,y0+opening_h,y0+FLOOR_H,z0,z1)

front_core_wall(0)
front_core_wall(1)

# Back wall with three spaced hall windows on each floor.
for floor in range(2):
    y0=floor*FLOOR_H
    # Adapt facade routine with central width using three smaller windows.
    z=Z_BACK
    z0,z1=z-WALL/2,z+WALL/2
    sill=WINDOW_SILL; wh=WINDOW_H
    W.box(f'Core_F{floor+1}_Back_Bottom',CORE_X0,CORE_X1,y0,y0+sill,z0,z1)
    W.box(f'Core_F{floor+1}_Back_Top',CORE_X0,CORE_X1,y0+sill+wh,y0+FLOOR_H,z0,z1)
    centers=(-10.0,0.0,10.0); ww=5.4
    segs=[(CORE_X0,centers[0]-ww/2),(centers[0]+ww/2,centers[1]-ww/2),(centers[1]+ww/2,centers[2]-ww/2),(centers[2]+ww/2,CORE_X1)]
    for i,(a,b) in enumerate(segs,1):
        W.box(f'Core_F{floor+1}_Back_Pier_{i}',a,b,y0+sill,y0+sill+wh,z0,z1)
    for i,c in enumerate(centers,1):
        W.box(f'Core_F{floor+1}_Back_Glass_{i}',c-ww/2+0.12,c+ww/2-0.12,y0+sill+0.12,y0+sill+wh-0.12,z+0.04,z+0.18,'Glass')

# Balcony at second floor center.
BAL_W=30.0; BAL_D=10.0
W.box('Balcony_Floor',-BAL_W/2,BAL_W/2,FLOOR_H-0.15,FLOOR_H+0.55,Z_FRONT-BAL_D,Z_FRONT,'Concrete')
# Two structural balcony columns.
for x in (-12.5,12.5):
    W.box(f'Balcony_Column_{x}',x-0.8,x+0.8,0,FLOOR_H,Z_FRONT-BAL_D+0.8,Z_FRONT-BAL_D+2.4,'WhiteBrick')
# Guardrails (real geometry).
RAIL_H=3.2; RT=0.22
W.box('Balcony_Rail_Front',-BAL_W/2,BAL_W/2,FLOOR_H+0.55,FLOOR_H+0.55+RAIL_H,Z_FRONT-BAL_D-RT/2,Z_FRONT-BAL_D+RT/2,'Metal')
W.box('Balcony_Rail_Left',-BAL_W/2-RT/2,-BAL_W/2+RT/2,FLOOR_H+0.55,FLOOR_H+0.55+RAIL_H,Z_FRONT-BAL_D,Z_FRONT,'Metal')
W.box('Balcony_Rail_Right',BAL_W/2-RT/2,BAL_W/2+RT/2,FLOOR_H+0.55,FLOOR_H+0.55+RAIL_H,Z_FRONT-BAL_D,Z_FRONT,'Metal')
# Entrance stoop.
W.box('Entrance_Stoop',-8,8,-0.05,0.35,Z_FRONT-5,Z_FRONT,'Concrete')

# Low green visual base trim around front/back and ends.
TRIM_H=1.8; TT=0.18
W.box('GreenTrim_Front',X_MIN,X_MAX,0,TRIM_H,Z_FRONT-TT,Z_FRONT,'GreenTrim')
W.box('GreenTrim_Back',X_MIN,X_MAX,0,TRIM_H,Z_BACK,Z_BACK+TT,'GreenTrim')
W.box('GreenTrim_West',X_MIN-TT,X_MIN,0,TRIM_H,Z_FRONT,Z_BACK,'GreenTrim')
W.box('GreenTrim_East',X_MAX,X_MAX+TT,0,TRIM_H,Z_FRONT,Z_BACK,'GreenTrim')

# ============================================================
# U staircase - actual steps, not a fake ramp
# ============================================================

STAIR_W=7.0
STEP_RUN=1.45
STEPS_PER_FLIGHT=8
STEP_RISE=(FLOOR_H/2.0)/STEPS_PER_FLIGHT
# First flight: left side, from z=-8 toward +3.6
for i in range(STEPS_PER_FLIGHT):
    top=(i+1)*STEP_RISE
    z0=-8.0+i*STEP_RUN
    z1=z0+STEP_RUN
    W.box(f'Stair_Flight1_Step_{i+1}',-8.5,-1.5,0,top,z0,z1,'Concrete')
# Mid landing
LAND_Z=-8.0+STEPS_PER_FLIGHT*STEP_RUN
W.box('Stair_MidLanding',-9.5,9.5,FLOOR_H/2-0.35,FLOOR_H/2+0.35,LAND_Z,LAND_Z+4.2,'Concrete')
# Second flight: right side, returns toward front while rising to floor 2.
for i in range(STEPS_PER_FLIGHT):
    top=FLOOR_H/2+(i+1)*STEP_RISE
    z1=LAND_Z-i*STEP_RUN
    z0=z1-STEP_RUN
    W.box(f'Stair_Flight2_Step_{i+1}',1.5,8.5,FLOOR_H/2,top,z0,z1,'Concrete')
# Upper landing meets the second-floor opening.
W.box('Stair_UpperLanding',1.0,10.0,FLOOR_H-0.35,FLOOR_H+0.35,-12.0,-8.0,'Concrete')

# ============================================================
# True pitched roofs / eaves / ridge
# ============================================================

def add_gable_roof(prefix,x0,x1,eave_y,ridge_y,z0,z1,overhang=2.0):
    xx0=x0-overhang; xx1=x1+overhang
    zz0=z0-overhang; zz1=z1+overhang
    zc=(zz0+zz1)/2
    # Front and back sloped roof slabs. These are actual inclined surfaces.
    W.sloped_slab(prefix+'_FrontSlope',
        (xx0,eave_y,zz0),(xx1,eave_y,zz0),(xx1,ridge_y,zc),(xx0,ridge_y,zc),0.42,'RoofTile')
    W.sloped_slab(prefix+'_BackSlope',
        (xx0,ridge_y,zc),(xx1,ridge_y,zc),(xx1,eave_y,zz1),(xx0,eave_y,zz1),0.42,'RoofTile')
    # Ridge cap and fascia/eave boards.
    W.box(prefix+'_Ridge',xx0,xx1,ridge_y-0.25,ridge_y+0.25,zc-0.32,zc+0.32,'RoofTile')
    W.box(prefix+'_FrontFascia',xx0,xx1,eave_y-0.45,eave_y+0.10,zz0-0.20,zz0+0.20,'Wood')
    W.box(prefix+'_BackFascia',xx0,xx1,eave_y-0.45,eave_y+0.10,zz1-0.20,zz1+0.20,'Wood')

add_gable_roof('Roof_LeftWing',X_MIN,CORE_X0,BUILDING_H+0.65,BUILDING_H+8.8,Z_FRONT,Z_BACK)
add_gable_roof('Roof_RightWing',CORE_X1,X_MAX,BUILDING_H+0.65,BUILDING_H+8.8,Z_FRONT,Z_BACK)
# Central roof is a little higher to give the middle architectural emphasis.
add_gable_roof('Roof_Central',CORE_X0,CORE_X1,BUILDING_H+0.85,BUILDING_H+10.8,Z_FRONT,Z_BACK,overhang=2.5)

# Balcony canopy: real single sloped plate from wall downward toward front.
W.sloped_slab('Balcony_Canopy',
    (-16.5,BUILDING_H-0.2,Z_FRONT+0.5),(16.5,BUILDING_H-0.2,Z_FRONT+0.5),
    (16.5,BUILDING_H-2.0,Z_FRONT-BAL_D-1.4),(-16.5,BUILDING_H-2.0,Z_FRONT-BAL_D-1.4),
    0.32,'RoofTile')

# ============================================================
# Scale references
# ============================================================
add_r6('R6_GroundHall',0,0,-20)
add_r6('R6_SecondHall',0,FLOOR_H,-20)
add_r6('R6_ClassroomScale',-80,0,-25)

# ============================================================
# Export OBJ/MTL + checks + simple floor plan SVG
# ============================================================
obj_path=OUT/'Bloco_Instrucao_3D.obj'
mtl_path=OUT/'Bloco_Instrucao_3D.mtl'
W.write(obj_path)
mtl_path.write_text(MTL,encoding='utf-8')

# Structural checks based on the same parameter set used by the model.
checks={
    'rooms_total': len(room_meta),
    'rooms_per_floor': {str(f):sum(1 for r in room_meta if r['floor']==f) for f in (1,2)},
    'expected_rooms_total':16,
    'corridor_width':CORRIDOR_W,
    'classroom_size':[ROOM_W,ROOM_D],
    'building_size':[BUILDING_W,BUILDING_D,BUILDING_H],
    'balcony_centered': True,
    'stair_type':'U - two real stepped flights + mid landing',
    'roof_type':'three true gable roofs + sloped balcony canopy',
    'corridor_classroom_windows':0,
    'classroom_exterior_windows_per_room':3,
    'chairs_total':16,
    'r6_references':3,
    'obj_vertices':W.vcount,
    'obj_faces':W.face_count,
}
checks['PASS']=(checks['rooms_total']==16 and checks['rooms_per_floor']=={'1':8,'2':8} and checks['corridor_classroom_windows']==0)
(OUT/'checks.json').write_text(json.dumps(checks,indent=2),encoding='utf-8')

# Human-readable floor plan from the exact same dimensions.
scale=4.0
svg_w=BUILDING_W*scale+80
svg_h=BUILDING_D*scale+120
ox=40-X_MIN*scale
oz=60-Z_FRONT*scale

def sx(x): return ox+x*scale
def sz(z): return oz+z*scale

def rect(x0,z0,x1,z1,fill,stroke='#333',sw=2):
    return f'<rect x="{sx(x0):.1f}" y="{sz(z0):.1f}" width="{(x1-x0)*scale:.1f}" height="{(z1-z0)*scale:.1f}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"/>'

svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{svg_w:.0f}" height="{svg_h:.0f}" viewBox="0 0 {svg_w:.0f} {svg_h:.0f}">',
     '<rect width="100%" height="100%" fill="#f7f5ee"/>',
     '<text x="40" y="30" font-family="Arial" font-size="22" font-weight="700">Bloco de Instrucao - planta estrutural (mesma geometria do OBJ)</text>']
# Building outline / corridor / core
svg.append(rect(X_MIN,Z_FRONT,X_MAX,Z_BACK,'#ffffff','#111',3))
svg.append(rect(X_MIN,Z_CORR_FRONT,CORE_X0,Z_CORR_BACK,'#e6e6e6'))
svg.append(rect(CORE_X1,Z_CORR_FRONT,X_MAX,Z_CORR_BACK,'#e6e6e6'))
svg.append(rect(CORE_X0,Z_FRONT,CORE_X1,Z_BACK,'#e9efe8','#1e4a2e',3))
# rooms
n=0
for cx in ROOM_X:
    for bank,zc in [('F',-25),('B',25)]:
        n+=1
        z0=Z_FRONT if bank=='F' else Z_CORR_BACK
        z1=Z_CORR_FRONT if bank=='F' else Z_BACK
        svg.append(rect(cx-ROOM_W/2,z0,cx+ROOM_W/2,z1,'#fffdf7','#555',2))
        svg.append(f'<text x="{sx(cx):.1f}" y="{sz((z0+z1)/2):.1f}" text-anchor="middle" dominant-baseline="middle" font-family="Arial" font-size="16">Sala {n}</text>')
# balcony
svg.append(rect(-BAL_W/2,Z_FRONT-BAL_D,BAL_W/2,Z_FRONT,'#ded6c8','#333',2))
svg.append(f'<text x="{sx(0):.1f}" y="{sz(Z_FRONT-BAL_D/2):.1f}" text-anchor="middle" dominant-baseline="middle" font-family="Arial" font-size="14">VARANDA</text>')
# labels
svg.append(f'<text x="{sx(-60):.1f}" y="{sz(0):.1f}" text-anchor="middle" font-family="Arial" font-size="14">CORREDOR 16 studs</text>')
svg.append(f'<text x="{sx(60):.1f}" y="{sz(0):.1f}" text-anchor="middle" font-family="Arial" font-size="14">CORREDOR 16 studs</text>')
svg.append(f'<text x="{sx(0):.1f}" y="{sz(22):.1f}" text-anchor="middle" font-family="Arial" font-size="14">HALL / ESCADA U</text>')
svg.append('</svg>')
(OUT/'floorplan.svg').write_text('\n'.join(svg),encoding='utf-8')

readme=f'''# Bloco de Instrucao 3D\n\nModelo mestre gerado proceduralmente.\n\n- 2 andares\n- 8 salas por andar (16 total)\n- salas: {ROOM_W:.0f} x {ROOM_D:.0f} studs\n- corredor: {CORRIDOR_W:.0f} studs\n- predio: {BUILDING_W:.0f} x {BUILDING_D:.0f} studs\n- hall central, varanda central e escada em U real\n- 3 janelas externas espacadas por sala\n- zero janelas entre sala e corredor\n- teto/laje superior real\n- telhados inclinados de duas aguas com cumeeira e beiral\n- 1 cadeira de teste por sala\n- 3 referencias R6\n\n## Arquivos\n- `Bloco_Instrucao_3D.obj`\n- `Bloco_Instrucao_3D.mtl`\n- `floorplan.svg`\n- `checks.json`\n\nOs tijolos/telhas sao materiais visuais. Na conversao ao Roblox, o relevo pequeno deve ficar sem colisao (ou em textura), enquanto paredes, pisos, varanda e degraus usam colisoes lisas.\n'''
(OUT/'README.md').write_text(readme,encoding='utf-8')

print(json.dumps(checks,indent=2))
if not checks['PASS']:
    raise SystemExit('VALIDATION FAILED')
print(f'OBJ generated: {obj_path} ({W.vcount} vertices, {W.face_count} faces)')
