from pathlib import Path

root = Path('cefet-trainer/app/src/main/java/com/cefettrainer/app')

# V10Activity: use the new exam engine and fix AlertDialog import.
p = root / 'V10Activity.java'
s = p.read_text(encoding='utf-8')
if 'import android.app.AlertDialog;' not in s:
    s = s.replace('import android.app.Activity;\n', 'import android.app.Activity;\nimport android.app.AlertDialog;\n', 1)
s = s.replace('MainActivity.class', 'ExamV10Activity.class')
p.write_text(s, encoding='utf-8')

# Exam results feed the same spaced-review / error notebook used by the learning engine.
p = root / 'ExamV10Activity.java'
s = p.read_text(encoding='utf-8')
old = '''private void recordTopic(Q q,boolean ok){String k="examTopic::"+q.subject+"||"+q.topic;int a=prefs.getInt(k+"::a",0)+1,c=prefs.getInt(k+"::c",0)+(ok?1:0);prefs.edit().putInt(k+"::a",a).putInt(k+"::c",c).apply();}'''
new = '''private void recordTopic(Q q,boolean ok){
        String topicKey=q.subject+"||"+q.topic;
        String k="examTopic::"+topicKey;
        int a=prefs.getInt(k+"::a",0)+1,c=prefs.getInt(k+"::c",0)+(ok?1:0);
        SharedPreferences.Editor e=prefs.edit().putInt(k+"::a",a).putInt(k+"::c",c);
        int m=prefs.getInt("m::"+topicKey,0);
        if(ok){
            if(m>0)e.putInt("m::"+topicKey,Math.min(5,m+1));
            long next=System.currentTimeMillis()+(m>=3?7L:3L)*24*60*60*1000;
            e.putLong("next::"+topicKey,next);
        }else{
            e.putInt("wrong::"+topicKey,prefs.getInt("wrong::"+topicKey,0)+1);
            e.putInt("m::"+topicKey,Math.max(0,m-1));
            e.putLong("next::"+topicKey,System.currentTimeMillis()+10*60*1000);
        }
        e.apply();
    }'''
if old not in s:
    raise SystemExit('recordTopic anchor not found')
s = s.replace(old, new, 1)
p.write_text(s, encoding='utf-8')

# Basic source-level invariants. These fail CI before wasting time on the Android build.
checks = {
    'V10Activity.java': ['CEFET Trainer v0.10', 'Exemplo • não vale pontos', 'Revisões e caderno de erros', 'ExamV10Activity.class'],
    'ExamV10Activity.java': ['Simulado CEFET — 30 questões', 'Português', 'Matemática', 'Lei de Ohm', 'Mercantilismo'],
    'EssayCoachActivity.java': ['Caligrafia difícil — mais tentativas', 'Comparar leituras', 'plausibility', 'Avaliação de treino'],
}
for name, needles in checks.items():
    text=(root/name).read_text(encoding='utf-8')
    for needle in needles:
        if needle not in text:
            raise SystemExit(f'{name}: missing invariant {needle!r}')

print('v0.10 source finalization OK')
