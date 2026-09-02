package com.cefettrainer.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import java.text.Normalizer;
import java.util.*;

/** CEFET Trainer v0.13: navegação sequencial real + aulas com estrutura por matéria. */
public class V13Activity extends Activity {
    private final int NAVY=Color.rgb(18,45,73), BLUE=Color.rgb(36,94,157), GREEN=Color.rgb(33,128,91),
            RED=Color.rgb(183,55,55), AMBER=Color.rgb(177,112,16), PURPLE=Color.rgb(108,72,145),
            BG=Color.rgb(245,247,250), TEXT=Color.rgb(28,38,48), MUTED=Color.rgb(82,94,105), SOFT=Color.rgb(235,240,246);

    private LinkedHashMap<String, StudyCatalog.Node> nodes;
    private LinkedHashMap<String, ArrayList<StudyCatalog.Node>> bySubject;
    private SharedPreferences prefs;
    private float fontBoost;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        prefs=getSharedPreferences("cefet_v13",MODE_PRIVATE);
        fontBoost=prefs.getBoolean("large_font",false)?2f:0f;
        nodes=StudyCatalog.create();
        index();
        home();
    }

    private void index(){
        bySubject=new LinkedHashMap<>();
        for(String s:StudyCatalog.SUBJECT_ORDER) bySubject.put(s,new ArrayList<>());
        for(StudyCatalog.Node n:nodes.values()) bySubject.computeIfAbsent(n.subject,k->new ArrayList<>()).add(n);
    }

    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);} private int sp(int v){return Math.round(v+fontBoost);}
    private TextView t(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp(size));v.setTextColor(TEXT);v.setPadding(dp(12),dp(8),dp(12),dp(8));v.setLineSpacing(dp(2),1.08f);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private TextView muted(String s,int size){TextView v=t(s,size,false);v.setTextColor(MUTED);return v;}
    private Button btn(String s,int color,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(sp(15));b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));b.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);b.setMinHeight(dp(50));b.setPadding(dp(14),dp(8),dp(14),dp(8));b.setOnClickListener(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));b.setLayoutParams(p);return b;}
    private Button small(String s,int color,View.OnClickListener l){Button b=btn(s,color,l);b.setTextSize(sp(13));b.setMinHeight(dp(44));return b;}
    private LinearLayout root(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setBackgroundColor(BG);x.setPadding(dp(16),dp(16),dp(16),dp(96));return x;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(14),dp(12),dp(14),dp(12));c.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,dp(7));c.setLayoutParams(p);return c;}
    private ScrollView page(LinearLayout x){x.addView(new Space(this),new LinearLayout.LayoutParams(1,dp(120)));ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setVerticalScrollBarEnabled(true);s.setScrollbarFadingEnabled(false);s.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);s.addView(x,new ScrollView.LayoutParams(-1,-2));return s;}
    private void crumb(LinearLayout x,String label,Runnable back){x.addView(small("← Voltar",NAVY,v->back.run()));x.addView(muted(label,13));}
    private ProgressBar bar(int p){ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(100);b.setProgress(Math.max(0,Math.min(100,p)));b.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(18)));return b;}

    private void home(){
        LinearLayout x=root();
        x.addView(t("CEFET Trainer — v0.13",29,true));
        x.addView(muted("Agora 'próximo' significa próximo de verdade. Cada matéria também usa uma estrutura de aula própria.",14));
        LinearLayout c=card();c.addView(t("Seu estudo",19,true));c.addView(t("Cobertura "+coverage(null)+"%  •  Domínio "+mastery(null)+"%  •  Precisão "+accuracy()+"%",15,false));c.addView(bar(readiness(null)));c.addView(muted("Prontidão interna: "+readiness(null)+"%. Não é nota oficial da prova.",12));x.addView(c);
        StudyCatalog.Node nx=recommend(); if(nx!=null){LinearLayout n=card();n.addView(t("Continuar de onde faz sentido",18,true));n.addView(t(nx.subject+" • "+nx.title,16,true));n.addView(muted(nx.summary,13));n.addView(small("Abrir",GREEN,v->lesson(nx,"subject")));x.addView(n);}
        x.addView(btn("Reconstruir minha base — em ordem",GREEN,v->foundation()));
        x.addView(btn("Aprender por matéria",BLUE,v->subjectList()));
        x.addView(btn("Buscar tópico ou palavra",PURPLE,v->search()));
        x.addView(btn("Revisões",AMBER,v->reviews()));
        x.addView(btn("Simulado — 30 questões",NAVY,v->startActivity(new Intent(this,RemasteredExamActivity.class))));
        x.addView(btn("Redação — foto/OCR + treino",PURPLE,v->startActivity(new Intent(this,EssayCoachActivity.class))));
        x.addView(small(prefs.getBoolean("large_font",false)?"Fonte maior: ligada":"Fonte maior: desligada",Color.DKGRAY,v->{boolean on=!prefs.getBoolean("large_font",false);prefs.edit().putBoolean("large_font",on).apply();fontBoost=on?2f:0f;home();}));
        setContentView(page(x));
    }

    private void subjectList(){LinearLayout x=root();crumb(x,"Início > matérias",this::home);x.addView(t("Matérias",27,true));for(String s:StudyCatalog.SUBJECT_ORDER){ArrayList<StudyCatalog.Node> list=bySubject.get(s);int count=list==null?0:list.size();x.addView(btn(s+" • "+readiness(s)+"% • "+count+" tópicos",BLUE,v->subject(s)));}setContentView(page(x));}
    private void subject(String s){LinearLayout x=root();crumb(x,"Início > "+s,this::subjectList);x.addView(t(s,27,true));LinearLayout intro=card();intro.addView(t("Mapa da matéria",18,true));intro.addView(t(StudyCatalog.subjectIntro(s),15,false));intro.addView(muted("Toque em qualquer tópico. Dentro da aula, Anterior/Próximo seguem exatamente esta lista.",12));x.addView(intro);ArrayList<StudyCatalog.Node> list=bySubject.get(s);if(list!=null){for(int i=0;i<list.size();i++){StudyCatalog.Node n=list.get(i);String st=nodeMastery(n.id)>=4?"forte":nodeMastery(n.id)>0?"em treino":"novo";x.addView(btn((i+1)+". "+n.title+" • "+nodePercent(n.id)+"% • "+st,nodeMastery(n.id)>=4?GREEN:BLUE,v->lesson(n,"subject")));}}setContentView(page(x));}

    private void foundation(){LinearLayout x=root();crumb(x,"Início > reconstrução",this::home);x.addView(t("Reconstrução da base",27,true));x.addView(muted("A ordem é fixa: um tópico leva ao seguinte. O botão Próximo nunca volta para a mesma aula.",14));int i=0;for(String id:StudyCatalog.FOUNDATION){StudyCatalog.Node n=nodes.get(id);if(n==null)continue;i++;x.addView(btn(String.format(Locale.getDefault(),"%02d. %s • %s • %d%%",i,n.subject,n.title,nodePercent(id)),nodeMastery(id)>=4?GREEN:BLUE,v->lesson(n,"foundation")));}setContentView(page(x));}

    private void lesson(StudyCatalog.Node n,String track){
        markSeen(n.id);LinearLayout x=root();
        Runnable back="foundation".equals(track)?this::foundation:()->subject(n.subject);
        crumb(x,"Início > "+n.subject+" > "+n.title,back);
        ArrayList<StudyCatalog.Node> seq=sequence(n,track);int idx=indexOf(seq,n.id);
        x.addView(t(n.title,26,true));
        x.addView(muted((idx>=0?"Tópico "+(idx+1)+" de "+seq.size()+" • ":"")+"nível "+n.level+" • domínio "+nodePercent(n.id)+"%",13));

        if(n.prerequisites.length>0){LinearLayout pre=card();pre.addView(t("Pré-requisitos deste tópico",17,true));for(String pid:n.prerequisites){StudyCatalog.Node p=nodes.get(pid);if(p!=null)pre.addView(small((nodeMastery(pid)<2?"Preciso revisar: ":"Base praticada: ")+p.title,nodeMastery(pid)<2?AMBER:GREEN,v->lesson(p,"subject")));}x.addView(pre);}

        renderSubjectLesson(x,n);

        LinearLayout nav=card();nav.addView(t("Navegação da trilha",18,true));
        if(idx>0){StudyCatalog.Node p=seq.get(idx-1);nav.addView(btn("← Anterior: "+p.title,NAVY,v->lesson(p,track)));}
        if(idx>=0&&idx<seq.size()-1){StudyCatalog.Node next=seq.get(idx+1);nav.addView(btn("Próximo: "+next.title+" →",GREEN,v->lesson(next,track)));}
        else nav.addView(btn("Fim desta trilha — voltar ao mapa",GREEN,v->back.run()));
        nav.addView(small("Marcar este tópico para revisar amanhã",AMBER,v->{schedule(n.id,1);Toast.makeText(this,"Revisão marcada.",Toast.LENGTH_SHORT).show();}));x.addView(nav);
        setContentView(page(x));
    }

    private void renderSubjectLesson(LinearLayout x,StudyCatalog.Node n){
        if("Português".equals(n.subject)) portuguese(x,n);
        else if("Matemática".equals(n.subject)) math(x,n);
        else if("História".equals(n.subject)) history(x,n);
        else if("Geografia".equals(n.subject)) geography(x,n);
        else science(x,n);
    }

    private void portuguese(LinearLayout x,StudyCatalog.Node n){
        LinearLayout a=card();a.addView(t("1. Entenda a palavra/ideia",18,true));a.addView(t(n.summary,16,false));a.addView(t("Guarde isto",15,true));a.addView(t(n.memory,17,true));x.addView(a);
        LinearLayout b=card();b.addView(t("2. Desmonte uma frase",18,true));b.addView(t(n.worked,15,false));b.addView(t("Vocabulário necessário",15,true));b.addView(t(n.terms,14,false));b.addView(small("Não sei uma palavra",PURPLE,v->wordHelp()));x.addView(b);
        questionCard(x,n,n.check,"3. Checagem no meio da aula","check");
        LinearLayout c=card();c.addView(t("4. O detalhe que muda a interpretação",18,true));c.addView(t(n.detail,15,false));c.addView(t("Armadilha",15,true));c.addView(t(n.trap,14,false));c.addView(t("Contraexemplo",15,true));c.addView(t(n.counterExample,14,false));x.addView(c);
        questionCard(x,n,n.transfer,"5. Agora em nível de transferência","transfer");
    }

    private void math(LinearLayout x,StudyCatalog.Node n){
        LinearLayout a=card();a.addView(t("1. Ideia matemática",18,true));a.addView(t(n.summary,16,false));a.addView(t("Regra curta para lembrar",15,true));a.addView(t(n.memory,17,true));x.addView(a);
        LinearLayout b=card();b.addView(t("2. Conta guiada, passo a passo",18,true));b.addView(t(n.worked,15,false));x.addView(b);
        questionCard(x,n,n.check,"3. Pare e calcule","check");
        LinearLayout c=card();c.addView(t("4. Por que a regra funciona",18,true));c.addView(t(n.detail,15,false));c.addView(t("Erro que derruba muita conta",15,true));c.addView(t(n.trap,14,false));c.addView(t("Quando NÃO aplicar assim",15,true));c.addView(t(n.counterExample,14,false));x.addView(c);
        questionCard(x,n,n.transfer,"5. Problema novo — sem copiar o exemplo","transfer");
    }

    private void science(LinearLayout x,StudyCatalog.Node n){
        LinearLayout a=card();a.addView(t("1. Fenômeno / conceito",18,true));a.addView(t(n.summary,16,false));a.addView(t("Resumo de memória",15,true));a.addView(t(n.memory,17,true));x.addView(a);
        LinearLayout b=card();b.addView(t("2. Modelo explicado",18,true));b.addView(t(n.detail,15,false));b.addView(t("Exemplo observado",15,true));b.addView(t(n.worked,15,false));x.addView(b);
        questionCard(x,n,n.check,"3. Teste de compreensão","check");
        LinearLayout c=card();c.addView(t("4. Limite do modelo / erro comum",18,true));c.addView(t(n.counterExample,14,false));c.addView(t(n.trap,14,false));x.addView(c);
        questionCard(x,n,n.transfer,"5. Aplicação em situação diferente","transfer");
    }

    private void history(LinearLayout x,StudyCatalog.Node n){
        LinearLayout a=card();a.addView(t("1. Contexto",18,true));a.addView(t(n.summary,16,false));x.addView(a);
        LinearLayout b=card();b.addView(t("2. Causa → acontecimento → consequência",18,true));b.addView(t(n.detail,15,false));b.addView(t("Exemplo de leitura histórica",15,true));b.addView(t(n.worked,15,false));x.addView(b);
        questionCard(x,n,n.check,"3. Checagem de contexto","check");
        LinearLayout c=card();c.addView(t("4. Cuidado com simplificações",18,true));c.addView(t(n.counterExample,14,false));c.addView(t(n.trap,14,false));c.addView(t("Para guardar",15,true));c.addView(t(n.memory,17,true));x.addView(c);
        questionCard(x,n,n.transfer,"5. Questão de interpretação histórica","transfer");
    }

    private void geography(LinearLayout x,StudyCatalog.Node n){
        LinearLayout a=card();a.addView(t("1. Localize a ideia",18,true));a.addView(t(n.summary,16,false));a.addView(t("Palavras do mapa/território",15,true));a.addView(t(n.terms,14,false));x.addView(a);
        LinearLayout b=card();b.addView(t("2. Relação espacial ou social",18,true));b.addView(t(n.detail,15,false));b.addView(t("Exemplo",15,true));b.addView(t(n.worked,15,false));x.addView(b);
        questionCard(x,n,n.check,"3. Teste rápido","check");
        LinearLayout c=card();c.addView(t("4. Pegadinha de leitura",18,true));c.addView(t(n.trap,14,false));c.addView(t(n.counterExample,14,false));c.addView(t("Resumo",15,true));c.addView(t(n.memory,17,true));x.addView(c);
        questionCard(x,n,n.transfer,"5. Transferência para outro contexto","transfer");
    }

    private void questionCard(LinearLayout x,StudyCatalog.Node n,StudyCatalog.Question q,String title,String phase){if(q==null)return;LinearLayout c=card();c.addView(t(title,18,true));renderQuestion(c,n,q,phase);x.addView(c);}

    private void renderQuestion(LinearLayout box,StudyCatalog.Node n,StudyCatalog.Question q,String phase){
        box.addView(t(q.prompt,16,true));RadioGroup g=new RadioGroup(this);g.setOrientation(RadioGroup.VERTICAL);ArrayList<RadioButton> radios=new ArrayList<>();HashMap<Integer,Integer> ids=new HashMap<>();
        for(int i=0;i<q.options.length;i++){RadioButton r=new RadioButton(this);int id=View.generateViewId();r.setId(id);ids.put(id,i);r.setText((char)('A'+i)+") "+q.options[i]);r.setTextSize(sp(15));r.setTextColor(TEXT);r.setPadding(dp(6),dp(7),dp(6),dp(7));g.addView(r);radios.add(r);}box.addView(g);
        LinearLayout fb=card();fb.setBackgroundColor(SOFT);fb.setVisibility(View.GONE);box.addView(fb);final boolean[] done={false};
        g.setOnCheckedChangeListener((group,checked)->{if(done[0])return;Integer chosen=ids.get(checked);if(chosen==null)return;done[0]=true;boolean ok=chosen==q.correct;for(int i=0;i<radios.size();i++){radios.get(i).setEnabled(false);if(i==q.correct){radios.get(i).setTextColor(GREEN);radios.get(i).setTypeface(Typeface.DEFAULT,Typeface.BOLD);}else if(i==chosen){radios.get(i).setTextColor(RED);radios.get(i).setTypeface(Typeface.DEFAULT,Typeface.BOLD);}}record(n.id,ok,phase);fb.setVisibility(View.VISIBLE);TextView r=t(ok?"✓ Certo":"✗ Não",18,true);r.setTextColor(ok?GREEN:RED);fb.addView(r);fb.addView(t("Sua escolha: "+q.feedback[chosen],14,false));if(!ok)fb.addView(t("Correta: "+q.options[q.correct]+" — "+q.feedback[q.correct],14,true));fb.addView(t("Por que as outras não servem",15,true));for(int i=0;i<q.options.length;i++){TextView why=muted((i==q.correct?"✓ ":"• ")+(char)('A'+i)+": "+q.feedback[i],12);if(i==q.correct)why.setTextColor(GREEN);fb.addView(why);}String route=chosen<q.route.length?q.route[chosen]:null;if(!ok&&route!=null&&nodes.containsKey(route)){StudyCatalog.Node p=nodes.get(route);fb.addView(t("Lacuna provável: "+p.title,15,true));fb.addView(small("Reparar essa base agora",AMBER,v->lesson(p,"subject")));}});
    }

    private ArrayList<StudyCatalog.Node> sequence(StudyCatalog.Node n,String track){ArrayList<StudyCatalog.Node> out=new ArrayList<>();if("foundation".equals(track)){for(String id:StudyCatalog.FOUNDATION){StudyCatalog.Node z=nodes.get(id);if(z!=null)out.add(z);}return out;}ArrayList<StudyCatalog.Node> l=bySubject.get(n.subject);if(l!=null)out.addAll(l);return out;}
    private int indexOf(ArrayList<StudyCatalog.Node> list,String id){for(int i=0;i<list.size();i++)if(list.get(i).id.equals(id))return i;return -1;}

    private void wordHelp(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);EditText in=new EditText(this);in.setHint("Ex.: singular, substantivo, razão...");box.addView(in);TextView out=t("Digite uma palavra.",14,false);box.addView(out);AlertDialog d=new AlertDialog.Builder(this).setTitle("Dicionário do estudo").setView(box).setPositiveButton("Procurar",null).setNegativeButton("Fechar",null).create();d.setOnShowListener(z->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String q=norm(in.getText().toString());String found=null,key=null;for(Map.Entry<String,String> e:StudyCatalog.glossary().entrySet()){if(!q.isEmpty()&&(norm(e.getKey()).contains(q)||q.contains(norm(e.getKey())))){key=e.getKey();found=e.getValue();break;}}out.setText(found==null?"Não achei esse termo ainda.":key.toUpperCase(Locale.ROOT)+"\n\n"+found);}));d.show();}

    private void search(){LinearLayout x=root();crumb(x,"Início > busca",this::home);x.addView(t("Buscar",26,true));EditText in=new EditText(this);in.setHint("substantivo, fração, equação, átomo...");x.addView(in);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);x.addView(r);in.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){renderSearch(r,s.toString());}public void afterTextChanged(Editable e){}});renderSearch(r,"");setContentView(page(x));}
    private void renderSearch(LinearLayout r,String raw){r.removeAllViews();String q=norm(raw);int shown=0;for(StudyCatalog.Node n:nodes.values()){String hay=norm(n.subject+" "+n.title+" "+n.summary+" "+n.terms);if(q.isEmpty()||hay.contains(q)){r.addView(small(n.subject+" • "+n.title,BLUE,v->lesson(n,"subject")));if(++shown>=(q.isEmpty()?12:30))break;}}if(shown==0)r.addView(muted("Nada encontrado.",14));}
    private void reviews(){LinearLayout x=root();crumb(x,"Início > revisões",this::home);x.addView(t("Revisões",26,true));int c=0;for(StudyCatalog.Node n:nodes.values())if(nodeMastery(n.id)>0&&due(n.id)){c++;x.addView(btn(n.subject+" • "+n.title,AMBER,v->lesson(n,"subject")));}if(c==0)x.addView(muted("Nenhuma revisão vencida agora.",14));setContentView(page(x));}

    private void markSeen(String id){prefs.edit().putBoolean("seen::"+id,true).apply();}
    private int nodeMastery(String id){return prefs.getInt("mastery::"+id,0);} private int nodePercent(String id){int m=nodeMastery(id),a=prefs.getInt("attempts::"+id,0),c=prefs.getInt("correct::"+id,0);int acc=a==0?0:Math.round(c*100f/a);return Math.min(100,(prefs.getBoolean("seen::"+id,false)?15:0)+m*13+Math.round(acc*.20f));}
    private void record(String id,boolean ok,String phase){int a=prefs.getInt("attempts",0)+1,c=prefs.getInt("correct",0)+(ok?1:0),na=prefs.getInt("attempts::"+id,0)+1,nc=prefs.getInt("correct::"+id,0)+(ok?1:0),m=nodeMastery(id);if(ok)m=Math.min(5,m+("transfer".equals(phase)?2:1));else if("transfer".equals(phase))m=Math.max(0,m-1);int days=ok?(m>=5?14:m>=4?7:m>=3?3:1):0;long now=System.currentTimeMillis();prefs.edit().putInt("attempts",a).putInt("correct",c).putInt("attempts::"+id,na).putInt("correct::"+id,nc).putInt("mastery::"+id,m).putLong("last::"+id,now).putLong("due::"+id,now+days*86400000L).apply();}
    private void schedule(String id,int days){prefs.edit().putLong("due::"+id,System.currentTimeMillis()+days*86400000L).apply();} private boolean due(String id){long d=prefs.getLong("due::"+id,0);return d>0&&System.currentTimeMillis()>=d;}
    private ArrayList<StudyCatalog.Node> filtered(String s){ArrayList<StudyCatalog.Node> l=new ArrayList<>();if(s==null)l.addAll(nodes.values());else if(bySubject.containsKey(s))l.addAll(bySubject.get(s));return l;}
    private int coverage(String s){ArrayList<StudyCatalog.Node> l=filtered(s);if(l.isEmpty())return 0;int n=0;for(StudyCatalog.Node x:l)if(prefs.getBoolean("seen::"+x.id,false))n++;return Math.round(n*100f/l.size());}
    private int mastery(String s){ArrayList<StudyCatalog.Node> l=filtered(s);if(l.isEmpty())return 0;int sum=0;for(StudyCatalog.Node x:l)sum+=nodeMastery(x.id)*20;return Math.round(sum/(float)l.size());}
    private int accuracy(){int a=prefs.getInt("attempts",0);return a==0?0:Math.round(prefs.getInt("correct",0)*100f/a);} private int subjectAccuracy(String s){int a=0,c=0;for(StudyCatalog.Node n:filtered(s)){a+=prefs.getInt("attempts::"+n.id,0);c+=prefs.getInt("correct::"+n.id,0);}return a==0?0:Math.round(c*100f/a);} private int readiness(String s){return Math.round(coverage(s)*.25f+mastery(s)*.50f+(s==null?accuracy():subjectAccuracy(s))*.25f);}
    private StudyCatalog.Node recommend(){for(String id:StudyCatalog.FOUNDATION){StudyCatalog.Node n=nodes.get(id);if(n!=null&&nodeMastery(id)<3)return n;}for(StudyCatalog.Node n:nodes.values())if(nodeMastery(n.id)<3)return n;return null;}
    private String norm(String s){if(s==null)return"";return Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).trim();}
}
