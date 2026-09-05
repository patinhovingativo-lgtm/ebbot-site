package com.cefettrainer.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.util.*;

/**
 * CEFET Trainer v0.10
 * Camada de aprendizagem e foco. O simulado objetivo legado continua em MainActivity.
 * Esta tela foi desenhada para ensinar antes de cobrar, diminuir carga visual e usar
 * recuperação ativa + revisão espaçada sem punição por erro.
 */
public class V10Activity extends Activity {
    private final int NAVY = Color.rgb(13,43,74);
    private final int BLUE = Color.rgb(28,91,150);
    private final int GREEN = Color.rgb(34,131,94);
    private final int RED = Color.rgb(183,61,61);
    private final int PURPLE = Color.rgb(123,76,155);
    private final int AMBER = Color.rgb(180,118,20);
    private final int BG = Color.rgb(246,248,251);
    private final int CARD = Color.WHITE;
    private final int TEXT = Color.rgb(27,38,49);
    private final int MUTED = Color.rgb(82,96,109);
    private final Random random = new Random();
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long focusEnd = 0L;
    private TextView focusClock;
    private final ArrayList<TopicRef> dailyQueue = new ArrayList<>();
    private int dailyIndex = 0;
    private float fontBoost = 0f;

    private static final String[] PORTUGUES = {
            "Compreensão global do texto","Localização de informação explícita","Inferência","Finalidade do texto","Tema e assunto","Tese","Argumento e evidência","Fato e opinião","Intertextualidade","Ironia e humor","Sentido literal e figurado","Denotação e conotação","Polissemia","Sinônimos e antônimos","Gêneros textuais","Tipologias textuais","Coesão referencial","Coesão sequencial","Conectivos","Pronomes e referenciação","Concordância nominal","Concordância verbal","Regência nominal","Regência verbal","Crase","Pontuação","Classes gramaticais","Formação de palavras","Período simples","Período composto","Coordenação","Subordinação","Vozes verbais","Discurso direto e indireto","Variação linguística","Singular e plural"
    };
    private static final String[] MATEMATICA = {
            "Conjuntos","Operações com conjuntos","Intervalos numéricos","Números naturais","Números inteiros","Números racionais","Números reais","Divisibilidade","Números primos","MDC e MMC","Frações","Números decimais","Potenciação","Radiciação","Notação científica","Razão","Proporção","Regra de três direta","Regra de três inversa","Porcentagem","Juros simples","Juros compostos","Expressões algébricas","Monômios e polinômios","Produtos notáveis","Fatoração","Equação do 1º grau","Sistema do 1º grau","Equação do 2º grau","Função: domínio, contradomínio e imagem","Função afim","Gráfico da função afim","Função quadrática","Gráfico da função quadrática","Ângulos","Triângulos","Polígonos","Semelhança de triângulos","Teorema de Tales","Teorema de Pitágoras","Circunferência e círculo","Perímetro","Áreas planas","Razões trigonométricas","Escala e unidades"
    };
    private static final String[] FISICA = {
            "Grandezas e unidades","Velocidade média","Movimento uniforme","Aceleração","Força e resultante","Leis de Newton","Peso e massa","Trabalho","Potência mecânica","Energia cinética","Energia potencial","Conservação de energia","Temperatura","Calor","Calorimetria","Mudanças de estado","Propagação do calor","Ondas","Som","Luz","Reflexão","Refração","Espelhos e lentes","Carga elétrica","Corrente elétrica","Tensão elétrica","Resistência elétrica","Lei de Ohm","Potência elétrica","Circuitos elétricos","Magnetismo"
    };
    private static final String[] QUIMICA = {
            "Matéria e energia","Propriedades da matéria","Estados físicos","Mudanças de estado","Substância e mistura","Misturas homogêneas e heterogêneas","Métodos de separação","Fenômeno físico e químico","Transformação química","Evidências de reação","Átomo","Próton, nêutron e elétron","Número atômico","Número de massa","Íons","Isótopos","Isóbaros","Isótonos","Isoeletrônicos","Tabela periódica","Famílias e períodos","Metais e ametais","Ligações químicas","Moléculas","Fórmulas químicas","Ácidos, bases, sais e óxidos","pH","Reações químicas","Conservação da massa"
    };
    private static final String[] BIOLOGIA = {
            "Características dos seres vivos","Níveis de organização","Célula procarionte e eucarionte","Membrana plasmática","Citoplasma","Núcleo","Organelas","Divisão celular","DNA e genes","Hereditariedade","Genótipo e fenótipo","Ecossistema","Habitat e nicho","Cadeia alimentar","Teia alimentar","Níveis tróficos","Relações ecológicas","Ciclos biogeoquímicos","Fluxo de energia","Biomas","Impactos ambientais","Efeito estufa","Biodiversidade","Seleção natural","Evolução","Sistema digestório","Sistema respiratório","Sistema circulatório","Sistema excretor","Sistema nervoso","Sistema endócrino","Sistema reprodutor","Saúde e prevenção","Biotecnologia"
    };
    private static final String[] HISTORIA = {
            "Formação do mundo moderno","Renascimento cultural","Humanismo","Renascimento científico","Reforma Protestante","Contrarreforma","Absolutismo","Estado moderno","Mercantilismo","Expansão marítima europeia","Pioneirismo português","Navegações espanholas","Conquista da América","Povos originários da América","Colonização espanhola","Colonização portuguesa","Brasil pré-colonial","Capitanias hereditárias","Governo-geral","Economia açucareira","Escravidão indígena","Escravidão africana","Tráfico atlântico","Sociedade colonial","União Ibérica","Invasões estrangeiras","Interiorização","Bandeirismo","Mineração","Sociedade mineradora","Administração colonial","Pacto colonial","Revoltas coloniais","Iluminismo","Revoluções inglesas","Revolução Industrial","Independência dos EUA","Revolução Francesa","Crise do sistema colonial","Independências na América"
    };
    private static final String[] GEOGRAFIA = {
            "Orientação e localização","Coordenadas geográficas","Escala cartográfica","Projeções cartográficas","Leitura de mapas","Fusos horários","Estrutura interna da Terra","Tectonismo","Vulcanismo","Terremotos","Relevo","Intemperismo","Solos","Tempo e clima","Elementos do clima","Fatores climáticos","Massas de ar","Climas do Brasil","Hidrografia","Bacias hidrográficas","Vegetação","Biomas brasileiros","Problemas ambientais","Recursos naturais","População absoluta e relativa","Crescimento populacional","Transição demográfica","Migrações","Urbanização","Rede urbana","Metropolização","Industrialização","Setores da economia","Espaço agrário","Estrutura fundiária","Agricultura","Pecuária","Relações de trabalho no campo","Globalização","Empresas transnacionais","Comércio mundial","Blocos econômicos","Divisão internacional do trabalho","Desigualdades socioespaciais","Geopolítica e território"
    };

    private static final LinkedHashMap<String,String[]> CURRICULUM = new LinkedHashMap<>();
    static {
        CURRICULUM.put("Português", PORTUGUES);
        CURRICULUM.put("Matemática", MATEMATICA);
        CURRICULUM.put("Física", FISICA);
        CURRICULUM.put("Química", QUIMICA);
        CURRICULUM.put("Biologia", BIOLOGIA);
        CURRICULUM.put("História", HISTORIA);
        CURRICULUM.put("Geografia", GEOGRAFIA);
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        prefs = getSharedPreferences("cefet_v10_learning", MODE_PRIVATE);
        fontBoost = prefs.getBoolean("large_font", false) ? 2f : 0f;
        home();
    }

    private int sp(int base){ return Math.round(base + fontBoost); }
    private TextView tv(String s, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp(size));
        v.setTextColor(TEXT);
        v.setPadding(12,10,12,10);
        v.setLineSpacing(2f,1.08f);
        if (bold) v.setTypeface(null,1);
        return v;
    }
    private TextView muted(String s, int size){ TextView v=tv(s,size,false); v.setTextColor(MUTED); return v; }
    private Button button(String s, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(sp(15));
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setMinHeight(54);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,6,0,6);
        b.setLayoutParams(p);
        return b;
    }
    private LinearLayout root() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(18,18,18,32);
        l.setBackgroundColor(BG);
        return l;
    }
    private ScrollView page(LinearLayout l){ ScrollView s=new ScrollView(this); s.setFillViewport(true); s.addView(l); return s; }
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(16,14,16,14);
        c.setBackgroundColor(CARD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,8,0,8);
        c.setLayoutParams(p);
        return c;
    }
    private void title(LinearLayout x, String name) {
        x.addView(button("← Início", NAVY, v->home()));
        x.addView(tv(name,26,true));
    }

    private void home() {
        stopTicker();
        LinearLayout x = root();
        x.addView(tv("CEFET Trainer v0.10",31,true));
        x.addView(muted("Aprender → testar sem pressão → treinar → revisar → simular",15));

        int learned=countAtLeast(1), solid=countAtLeast(3), total=countTopics(), due=countDue();
        LinearLayout stats=card();
        stats.addView(tv("Seu mapa de estudo",19,true));
        stats.addView(tv(learned+" / "+total+" tópicos iniciados",16,false));
        stats.addView(tv(solid+" tópicos com domínio forte",16,false));
        stats.addView(tv(due+" revisões disponíveis agora",16,false));
        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pb.setMax(Math.max(1,total)); pb.setProgress(solid); stats.addView(pb,new LinearLayout.LayoutParams(-1,28));
        x.addView(stats);

        LinearLayout next=card();
        next.addView(tv("Próximo passo",20,true));
        TopicRef recommendation=pickRecommendation();
        next.addView(tv(recommendation==null?"Comece pelo diagnóstico rápido.":recommendation.subject+" • "+recommendation.topic,17,true));
        next.addView(muted("O aplicativo prioriza revisão vencida, ponto fraco e conteúdo ainda não estudado. Errar durante aprendizagem não tira pontos.",14));
        x.addView(next);

        x.addView(button("Plano de hoje — 3 blocos",GREEN,v->startDailyPlan()));
        x.addView(button("Aprender por matéria",BLUE,v->subjects()));
        x.addView(button("Revisões e caderno de erros",AMBER,v->reviewHub()));
        x.addView(button("Simulado no formato CEFET",NAVY,v->startActivity(new Intent(this,MainActivity.class))));
        x.addView(button("Redação — leitor e treinador",PURPLE,v->startActivity(new Intent(this,EssayCoachActivity.class))));
        x.addView(button("Modo foco / ritmo",Color.rgb(73,107,121),v->focusHub()));
        x.addView(button("Glossário — não sei o que isso significa",Color.rgb(82,98,171),v->glossary()));
        x.addView(button("Progresso detalhado",Color.rgb(96,105,113),v->progressHub()));
        setContentView(page(x));
    }

    private void subjects(){
        LinearLayout x=root(); title(x,"Aprender por matéria");
        x.addView(muted("Escolha uma matéria. Dentro dela, cada tópico começa com explicação e exemplo. O exemplo não vale pontos.",14));
        for(String s:CURRICULUM.keySet()){
            int done=countSubjectAtLeast(s,1), total=CURRICULUM.get(s).length;
            x.addView(button(s+"  •  "+done+"/"+total, BLUE, v->topicList(s)));
        }
        setContentView(page(x));
    }

    private void topicList(String subject){
        LinearLayout x=root(); title(x,subject);
        x.addView(muted("Legenda: ○ novo   ◔ iniciado   ● domínio forte   ↻ revisar",14));
        String[] arr=CURRICULUM.get(subject);
        for(String t:arr){
            int m=mastery(subject,t); boolean due=isDue(subject,t);
            String icon=due&&m>0?"↻":m>=3?"●":m>0?"◔":"○";
            int color=due?AMBER:(m>=3?GREEN:BLUE);
            x.addView(button(icon+"  "+t,color,v->lessonScreen(new TopicRef(subject,t))));
        }
        setContentView(page(x));
    }

    private void lessonScreen(TopicRef ref){
        Lesson l=lessonFor(ref.subject,ref.topic);
        LinearLayout x=root(); title(x,ref.subject+" • "+ref.topic);
        LinearLayout c=card();
        c.addView(tv("1. O que isso é?",19,true)); c.addView(tv(l.what,16,false));
        c.addView(tv("2. O que preciso guardar?",19,true)); c.addView(tv(l.remember,16,false));
        c.addView(tv("3. Palavras que podem aparecer",19,true)); c.addView(tv(l.words,15,false));
        c.addView(tv("4. Como resolver",19,true)); c.addView(tv(l.steps,15,false));
        c.addView(tv("5. Erro comum",19,true)); c.addView(tv(l.trap,15,false));
        x.addView(c);
        LinearLayout solved=card();
        solved.addView(tv("Exemplo resolvido",19,true));
        solved.addView(tv(l.worked,16,false));
        x.addView(solved);
        x.addView(button("Fazer exemplo sem valer pontos",GREEN,v->exampleScreen(ref,l)));
        x.addView(button("Estou travado — simplificar",AMBER,v->simplify(ref,l)));
        x.addView(button("Marcar para revisar depois",Color.rgb(105,111,118),v->{schedule(ref,1);Toast.makeText(this,"Revisão marcada.",Toast.LENGTH_SHORT).show();}));
        setContentView(page(x));
    }

    private void simplify(TopicRef ref, Lesson l){
        LinearLayout x=root(); title(x,"Versão curta • "+ref.topic);
        LinearLayout c=card();
        c.addView(tv("Só faça isto agora:",20,true));
        c.addView(tv(l.microStep,18,false));
        c.addView(muted("Não precisa dominar o assunto inteiro de uma vez. Entenda esta parte, teste um exemplo e depois avance.",15));
        x.addView(c);
        x.addView(button("Entendi esta parte — ver exemplo",GREEN,v->exampleScreen(ref,l)));
        x.addView(button("Voltar à explicação completa",BLUE,v->lessonScreen(ref)));
        setContentView(page(x));
    }

    private void exampleScreen(TopicRef ref, Lesson l){
        Example e=exampleFor(ref,l);
        LinearLayout x=root(); title(x,"Exemplo • não vale pontos");
        x.addView(muted("Você pode errar. O objetivo aqui é descobrir o raciocínio antes do treino.",14));
        LinearLayout q=card(); q.addView(tv(e.question,18,true));
        RadioGroup g=new RadioGroup(this); g.setOrientation(RadioGroup.VERTICAL);
        ArrayList<RadioButton> rbs=new ArrayList<>();
        for(int i=0;i<4;i++){
            RadioButton rb=new RadioButton(this); rb.setId(700+i); rb.setText((char)('A'+i)+") "+e.options[i]); rb.setTextSize(sp(16)); rb.setPadding(8,9,8,9); g.addView(rb); rbs.add(rb);
        }
        q.addView(g); x.addView(q);
        LinearLayout feedback=card(); feedback.setVisibility(View.GONE); x.addView(feedback);
        Button check=button("Verificar e entender",GREEN,v->{
            int id=g.getCheckedRadioButtonId(); if(id==-1){Toast.makeText(this,"Escolha uma alternativa.",Toast.LENGTH_SHORT).show();return;}
            int chosen=id-700;
            for(int i=0;i<rbs.size();i++){
                if(i==e.correct){rbs.get(i).setTextColor(GREEN);rbs.get(i).setTypeface(null,1);}
                else if(i==chosen){rbs.get(i).setTextColor(RED);rbs.get(i).setTypeface(null,1);}
                rbs.get(i).setEnabled(false);
            }
            feedback.removeAllViews(); feedback.setVisibility(View.VISIBLE);
            if(chosen==e.correct){
                feedback.addView(tv("✓ Acertou",20,true));
                TextView why=tv("Por que está certo: "+e.correctWhy,15,false); why.setTextColor(GREEN); feedback.addView(why);
                recordAttempt(ref,true);
            } else {
                feedback.addView(tv("✗ Esta alternativa não funciona",20,true));
                TextView bad=tv("Por que sua escolha está errada: "+e.wrongWhy[chosen],15,false); bad.setTextColor(RED); feedback.addView(bad);
                TextView good=tv("Por que a correta funciona: "+e.correctWhy,15,false); good.setTextColor(GREEN); feedback.addView(good);
                recordAttempt(ref,false);
            }
            feedback.addView(muted("Este exemplo não altera sua pontuação. Agora diga o quanto você entendeu para o app escolher a próxima revisão.",14));
            feedback.addView(button("Ainda não entendi",RED,v2->{markUnderstanding(ref,0);lessonScreen(ref);}));
            feedback.addView(button("Quase entendi",AMBER,v2->{markUnderstanding(ref,1);advanceAfterExample(ref);}));
            feedback.addView(button("Entendi e consigo explicar",GREEN,v2->{markUnderstanding(ref,2);retrievalPrompt(ref,l);}));
        });
        x.addView(check);
        setContentView(page(x));
    }

    private void retrievalPrompt(TopicRef ref, Lesson l){
        LinearLayout x=root(); title(x,"Explique sem olhar");
        x.addView(muted("Recuperar a ideia da memória ajuda mais do que apenas reler. Escreva em palavras simples; não há nota automática.",14));
        EditText ed=new EditText(this); ed.setMinLines(5); ed.setGravity(Gravity.TOP); ed.setTextSize(sp(16)); ed.setHint("Ex.: domínio é...; para resolver eu primeiro..."); ed.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); x.addView(ed,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout reveal=card(); reveal.setVisibility(View.GONE); x.addView(reveal);
        x.addView(button("Comparar com o essencial",BLUE,v->{
            reveal.setVisibility(View.VISIBLE); reveal.removeAllViews(); reveal.addView(tv("Compare sua explicação com isto:",18,true)); reveal.addView(tv(l.remember,16,false)); reveal.addView(muted("Não precisa usar as mesmas palavras. Verifique se a ideia principal e o procedimento aparecem.",14));
        }));
        x.addView(button("Consegui explicar",GREEN,v->{boostMastery(ref,1);schedule(ref,3);advanceAfterExample(ref);}));
        x.addView(button("Ainda ficou confuso",AMBER,v->{schedule(ref,0);lessonScreen(ref);}));
        setContentView(page(x));
    }

    private void advanceAfterExample(TopicRef ref){
        if(!dailyQueue.isEmpty() && dailyIndex<dailyQueue.size()-1){dailyIndex++;lessonScreen(dailyQueue.get(dailyIndex));}
        else if(!dailyQueue.isEmpty()){dailyQueue.clear();dailyIndex=0;dailyDone();}
        else topicList(ref.subject);
    }

    private void startDailyPlan(){
        dailyQueue.clear(); dailyIndex=0;
        HashSet<String> used=new HashSet<>();
        TopicRef due=pickDue(); if(due!=null){dailyQueue.add(due);used.add(key(due));}
        TopicRef weak=pickWeak(used); if(weak!=null){dailyQueue.add(weak);used.add(key(weak));}
        TopicRef fresh=pickFresh(used); if(fresh!=null){dailyQueue.add(fresh);used.add(key(fresh));}
        while(dailyQueue.size()<3){TopicRef any=randomTopic(); if(any==null||used.contains(key(any)))break; dailyQueue.add(any);used.add(key(any));}
        if(dailyQueue.isEmpty()){Toast.makeText(this,"Não encontrei tópicos. Abra uma matéria.",Toast.LENGTH_SHORT).show();return;}
        lessonScreen(dailyQueue.get(0));
    }

    private void dailyDone(){
        LinearLayout x=root(); title(x,"Plano concluído");
        LinearLayout c=card(); c.addView(tv("✓ Três blocos concluídos",24,true)); c.addView(tv("Você estudou, tentou recuperar da memória e programou revisões. Não é necessário fazer uma maratona para o estudo contar.",16,false)); x.addView(c);
        x.addView(button("Fazer simulado",NAVY,v->startActivity(new Intent(this,MainActivity.class))));
        x.addView(button("Encerrar por hoje",GREEN,v->home()));
        setContentView(page(x));
    }

    private void reviewHub(){
        LinearLayout x=root(); title(x,"Revisões e caderno de erros");
        ArrayList<TopicRef> due=listDue();
        ArrayList<TopicRef> errors=listErrors();
        x.addView(tv("Revisões disponíveis agora: "+due.size(),20,true));
        if(due.isEmpty()) x.addView(muted("Nenhuma revisão venceu ainda. O app volta a mostrar um tópico quando chega a hora de recuperá-lo da memória.",14));
        for(int i=0;i<Math.min(12,due.size());i++){TopicRef t=due.get(i);x.addView(button("↻ "+t.subject+" • "+t.topic,AMBER,v->lessonScreen(t)));}
        x.addView(tv("Tópicos com erros recentes",20,true));
        if(errors.isEmpty())x.addView(muted("Ainda não há erros registrados nos exemplos.",14));
        for(int i=0;i<Math.min(12,errors.size());i++){TopicRef t=errors.get(i);int n=prefs.getInt("wrong::"+key(t),0);x.addView(button("×"+n+"  "+t.subject+" • "+t.topic,RED,v->lessonScreen(t)));}
        setContentView(page(x));
    }

    private void focusHub(){
        LinearLayout x=root(); title(x,"Modo foco / ritmo");
        LinearLayout c=card(); c.addView(tv("Escolha um bloco pequeno",20,true)); c.addView(muted("Cronômetro serve para delimitar o estudo, não para punir. Você pode esconder ou encerrar a qualquer momento.",14)); x.addView(c);
        focusClock=tv("Sem cronômetro ativo",28,true); focusClock.setGravity(Gravity.CENTER); x.addView(focusClock);
        x.addView(button("8 minutos — começar pequeno",GREEN,v->startFocus(8)));
        x.addView(button("15 minutos — foco padrão",BLUE,v->startFocus(15)));
        x.addView(button("25 minutos — bloco longo",NAVY,v->startFocus(25)));
        x.addView(button("Pausa de 2 minutos",AMBER,v->startFocus(2)));
        x.addView(button("Parar cronômetro",RED,v->{focusEnd=0;stopTicker();focusClock.setText("Cronômetro parado");}));
        x.addView(button("Começar plano de hoje",GREEN,v->startDailyPlan()));
        x.addView(button(prefs.getBoolean("large_font",false)?"Fonte grande: ligada":"Fonte grande: desligada",Color.rgb(98,109,118),v->{boolean n=!prefs.getBoolean("large_font",false);prefs.edit().putBoolean("large_font",n).apply();fontBoost=n?2f:0f;focusHub();}));
        setContentView(page(x));
    }

    private void startFocus(int minutes){
        focusEnd=System.currentTimeMillis()+minutes*60_000L;
        stopTicker();
        handler.post(ticker);
    }
    private final Runnable ticker=new Runnable(){@Override public void run(){
        if(focusClock==null)return;
        long left=focusEnd-System.currentTimeMillis();
        if(left<=0){focusClock.setText("Bloco concluído ✓");focusEnd=0;Toast.makeText(V10Activity.this,"Bloco concluído. Você pode parar ou escolher o próximo passo.",Toast.LENGTH_LONG).show();return;}
        long s=left/1000;focusClock.setText(String.format(Locale.getDefault(),"%02d:%02d",s/60,s%60));handler.postDelayed(this,500);
    }};
    private void stopTicker(){handler.removeCallbacks(ticker);}

    private void glossary(){
        LinearLayout x=root(); title(x,"Glossário");
        x.addView(muted("Digite uma palavra da aula ou da questão. O objetivo é impedir que uma palavra desconhecida trave todo o problema.",14));
        EditText search=new EditText(this); search.setHint("Ex.: singular, inferência, domínio, isótopo..."); search.setSingleLine(true); x.addView(search,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout result=card(); result.addView(muted("Digite uma palavra e toque em pesquisar.",15)); x.addView(result);
        x.addView(button("Pesquisar significado",BLUE,v->{String q=search.getText().toString().trim();result.removeAllViews();Gloss g=gloss(q);result.addView(tv(g.term,20,true));result.addView(tv(g.meaning,16,false));result.addView(tv("Exemplo: "+g.example,15,false));}));
        x.addView(tv("Atalhos",18,true));
        String[] shortcuts={"singular","inferência","domínio","contradomínio","imagem","tese","coesão","proporção","isótopo","mercantilismo","globalização"};
        for(String s:shortcuts)x.addView(button(s,Color.rgb(82,98,171),v->{search.setText(s);Gloss g=gloss(s);result.removeAllViews();result.addView(tv(g.term,20,true));result.addView(tv(g.meaning,16,false));result.addView(tv("Exemplo: "+g.example,15,false));}));
        setContentView(page(x));
    }

    private void progressHub(){
        LinearLayout x=root(); title(x,"Progresso detalhado");
        int total=countTopics(), started=countAtLeast(1), strong=countAtLeast(3), due=countDue(), attempts=prefs.getInt("attempts",0), correct=prefs.getInt("correct",0);
        LinearLayout c=card(); c.addView(tv("Cobertura do conteúdo",20,true)); c.addView(tv(started+" / "+total+" tópicos iniciados",17,false)); c.addView(tv(strong+" com domínio forte",17,false)); c.addView(tv(due+" revisões disponíveis",17,false)); x.addView(c);
        LinearLayout e=card(); e.addView(tv("Exemplos de aprendizagem",20,true)); e.addView(tv(attempts+" tentativas",17,false)); e.addView(tv(correct+" acertos",17,false)); if(attempts>0)e.addView(tv(Math.round(correct*100f/attempts)+"% de acerto",17,false)); x.addView(e);
        x.addView(tv("Por matéria",20,true));
        for(String s:CURRICULUM.keySet()){int st=countSubjectAtLeast(s,1), so=countSubjectAtLeast(s,3);x.addView(tv(s+": "+st+" iniciados • "+so+" fortes • "+CURRICULUM.get(s).length+" total",15,false));}
        x.addView(muted("Domínio no app é um indicador de treino, não previsão das perguntas exatas da prova. O objetivo é cobrir o programa e treinar o tipo de raciocínio.",14));
        x.addView(button("Apagar apenas progresso de aprendizagem",RED,v->{new AlertDialog.Builder(this).setTitle("Apagar progresso?").setMessage("Isso zera domínio, revisões e erros deste sistema de aprendizagem.").setNegativeButton("Cancelar",null).setPositiveButton("Apagar",(d,w)->{prefs.edit().clear().apply();home();}).show();}));
        setContentView(page(x));
    }

    private void recordAttempt(TopicRef ref, boolean correct){
        SharedPreferences.Editor e=prefs.edit();
        e.putInt("attempts",prefs.getInt("attempts",0)+1);
        if(correct)e.putInt("correct",prefs.getInt("correct",0)+1);
        else e.putInt("wrong::"+key(ref),prefs.getInt("wrong::"+key(ref),0)+1);
        e.apply();
    }
    private void markUnderstanding(TopicRef ref,int level){
        int old=mastery(ref.subject,ref.topic), next=old;
        if(level==0){next=Math.max(0,old-1);schedule(ref,0);}
        if(level==1){next=Math.max(1,old);schedule(ref,1);}
        if(level==2){next=Math.min(5,Math.max(2,old+1));schedule(ref,next>=3?3:2);}
        prefs.edit().putInt("m::"+key(ref),next).apply();
    }
    private void boostMastery(TopicRef ref,int delta){prefs.edit().putInt("m::"+key(ref),Math.min(5,mastery(ref.subject,ref.topic)+delta)).apply();}
    private void schedule(TopicRef ref,int stage){
        long now=System.currentTimeMillis(); long when;
        if(stage<=0)when=now+10*60_000L;
        else if(stage==1)when=now+24*60*60_000L;
        else if(stage==2)when=now+3L*24*60*60_000L;
        else if(stage==3)when=now+7L*24*60*60_000L;
        else when=now+21L*24*60*60_000L;
        prefs.edit().putLong("next::"+key(ref),when).apply();
    }
    private int mastery(String s,String t){return prefs.getInt("m::"+key(new TopicRef(s,t)),0);}
    private boolean isDue(String s,String t){long n=prefs.getLong("next::"+key(new TopicRef(s,t)),Long.MAX_VALUE);return n<=System.currentTimeMillis();}
    private String key(TopicRef t){return t.subject+"||"+t.topic;}
    private int countTopics(){int n=0;for(String[] a:CURRICULUM.values())n+=a.length;return n;}
    private int countAtLeast(int level){int n=0;for(String s:CURRICULUM.keySet())for(String t:CURRICULUM.get(s))if(mastery(s,t)>=level)n++;return n;}
    private int countSubjectAtLeast(String s,int level){int n=0;for(String t:CURRICULUM.get(s))if(mastery(s,t)>=level)n++;return n;}
    private int countDue(){return listDue().size();}
    private ArrayList<TopicRef> allTopics(){ArrayList<TopicRef>a=new ArrayList<>();for(String s:CURRICULUM.keySet())for(String t:CURRICULUM.get(s))a.add(new TopicRef(s,t));return a;}
    private ArrayList<TopicRef> listDue(){ArrayList<TopicRef>a=new ArrayList<>();for(TopicRef t:allTopics())if(mastery(t.subject,t.topic)>0&&isDue(t.subject,t.topic))a.add(t);Collections.sort(a,(u,v)->Long.compare(prefs.getLong("next::"+key(u),0),prefs.getLong("next::"+key(v),0)));return a;}
    private ArrayList<TopicRef> listErrors(){ArrayList<TopicRef>a=new ArrayList<>();for(TopicRef t:allTopics())if(prefs.getInt("wrong::"+key(t),0)>0)a.add(t);Collections.sort(a,(u,v)->Integer.compare(prefs.getInt("wrong::"+key(v),0),prefs.getInt("wrong::"+key(u),0)));return a;}
    private TopicRef pickDue(){ArrayList<TopicRef>a=listDue();return a.isEmpty()?null:a.get(0);}
    private TopicRef pickWeak(Set<String> used){ArrayList<TopicRef>a=allTopics();Collections.shuffle(a,random);TopicRef best=null;int bm=99;for(TopicRef t:a){if(used.contains(key(t)))continue;int m=mastery(t.subject,t.topic);if(m>0&&m<bm){bm=m;best=t;}}return best;}
    private TopicRef pickFresh(Set<String> used){ArrayList<TopicRef>a=allTopics();Collections.shuffle(a,random);for(TopicRef t:a)if(!used.contains(key(t))&&mastery(t.subject,t.topic)==0)return t;return null;}
    private TopicRef randomTopic(){ArrayList<TopicRef>a=allTopics();return a.isEmpty()?null:a.get(random.nextInt(a.size()));}
    private TopicRef pickRecommendation(){TopicRef d=pickDue();if(d!=null)return d;TopicRef w=pickWeak(Collections.emptySet());if(w!=null)return w;return pickFresh(Collections.emptySet());}

    private Lesson lessonFor(String subject,String topic){
        String low=topic.toLowerCase(Locale.ROOT);
        if(low.contains("domínio")||low.contains("contradomínio")||low.contains("imagem")){
            return new Lesson(
                    "Uma função relaciona cada valor de entrada do domínio a exatamente um valor de saída. O contradomínio é o conjunto permitido de saídas; a imagem são as saídas que realmente aparecem.",
                    "Domínio = valores que entram. Contradomínio = conjunto onde as saídas podem estar. Imagem = valores que a função realmente produziu.",
                    "domínio (D), contradomínio (CD), imagem (Im), variável independente x, variável dependente f(x)",
                    "1) identifique os valores que podem entrar; 2) aplique a regra da função; 3) anote as saídas obtidas; 4) diferencie as saídas possíveis das que realmente apareceram.",
                    "Confundir imagem com contradomínio. A imagem pode ser apenas parte do contradomínio.",
                    "Se f(x)=2x e D={1,2,3,4}, então f(1)=2, f(2)=4, f(3)=6 e f(4)=8. Logo Im={2,4,6,8}.",
                    "Pegue um único valor do domínio e aplique a regra. Se x=3 em f(x)=2x, a saída é 6."
            );
        }
        if(low.contains("singular")||low.contains("plural")){
            return new Lesson(
                    "Singular indica uma unidade; plural indica duas ou mais. Isso afeta substantivos, artigos, adjetivos e a concordância da frase.",
                    "Singular = um/uma. Plural = mais de um. Observe quem é o núcleo e faça as palavras relacionadas concordarem com ele.",
                    "singular, plural, número gramatical, substantivo, artigo, adjetivo, concordância",
                    "1) encontre a palavra principal; 2) veja se ela está no singular ou plural; 3) confira se as palavras que dependem dela acompanham esse número.",
                    "Olhar apenas para a palavra imediatamente antes do verbo e ignorar o núcleo real do sujeito.",
                    "Em 'Os alunos atentos estudam', alunos está no plural; por isso 'os', 'atentos' e 'estudam' acompanham o plural.",
                    "Pergunte: estou falando de uma coisa/pessoa ou de mais de uma?"
            );
        }
        if(low.contains("inferência")){
            return new Lesson(
                    "Inferir é concluir algo que o texto não escreveu literalmente, mas que é sustentado pelas pistas presentes nele.",
                    "A resposta precisa nascer do texto. Inferência não é opinião pessoal nem adivinhação.",
                    "pista textual, implícito, conclusão, contexto, intenção",
                    "1) leia a pergunta; 2) localize a parte do texto relacionada; 3) junte duas ou mais pistas; 4) elimine alternativas que acrescentam informação sem apoio.",
                    "Escolher uma alternativa apenas porque parece verdadeira no mundo real, mesmo sem apoio no texto.",
                    "Se o texto diz que Ana saiu com guarda-chuva e voltou com a roupa seca enquanto chovia, podemos inferir que o guarda-chuva a protegeu, embora isso não esteja escrito dessa forma.",
                    "Sublinhe duas pistas que sustentem a conclusão."
            );
        }
        if(low.contains("porcentagem")){
            return new Lesson(
                    "Porcentagem compara uma parte com um total de 100. 25% significa 25 em cada 100, ou 0,25 do total.",
                    "Para achar p% de um valor V, use V×p/100. Para descobrir aumento ou desconto, identifique primeiro sobre qual valor a porcentagem é aplicada.",
                    "percentual, taxa, desconto, acréscimo, valor inicial, valor final",
                    "1) identifique o total de referência; 2) transforme a porcentagem em fração/decimal; 3) calcule a parte; 4) só depois some ou subtraia se houver aumento/desconto.",
                    "Aplicar duas porcentagens sucessivas como se fossem uma única soma. A segunda geralmente incide sobre um novo valor.",
                    "Um produto de R$ 200 com 15% de desconto: 200×0,15=30; preço final = 200−30=170.",
                    "Primeiro responda: '15% de quê?'. Depois calcule."
            );
        }
        if(low.contains("proporção")||low.contains("regra de três")||low.equals("razão")){
            return new Lesson(
                    "Razão compara duas grandezas; proporção afirma que duas razões são equivalentes. Regra de três é uma ferramenta para encontrar um valor desconhecido quando existe proporcionalidade.",
                    "Antes de multiplicar cruzado, descubra se as grandezas são diretamente ou inversamente proporcionais.",
                    "razão, proporção, diretamente proporcional, inversamente proporcional, constante de proporcionalidade",
                    "1) organize as grandezas; 2) teste a relação: se uma aumenta, a outra aumenta ou diminui? 3) monte a igualdade adequada; 4) resolva e confira se o resultado faz sentido.",
                    "Usar regra de três direta em uma situação inversa, como mais trabalhadores para menos tempo.",
                    "Se 3 cadernos custam R$ 24 no mesmo preço unitário, 5 custam 24/3×5 = R$ 40.",
                    "Descubra primeiro o valor de uma unidade."
            );
        }
        if(low.contains("lei de ohm")||low.contains("resistência elétrica")){
            return new Lesson(
                    "A Lei de Ohm relaciona tensão, corrente e resistência em um componente ôhmico: U = R·I.",
                    "U em volts, R em ohms e I em ampères. Isole a grandeza pedida antes de colocar números.",
                    "tensão U/V, corrente I, resistência R, volt, ampère, ohm",
                    "1) liste os dados com unidades; 2) escreva U=R·I; 3) isole a incógnita; 4) substitua; 5) verifique unidade e ordem de grandeza.",
                    "Misturar potência P com tensão U ou esquecer de converter miliampère para ampère.",
                    "Com U=12 V e R=6 Ω, I=U/R=12/6=2 A.",
                    "Escreva U=R·I e cubra mentalmente a letra que quer descobrir."
            );
        }
        if(low.contains("isótop")){
            return new Lesson(
                    "Isótopos são átomos do mesmo elemento: têm o mesmo número de prótons, mas diferente número de nêutrons e, por isso, diferente número de massa.",
                    "Mesmo Z = mesmo elemento. Isótopos: mesmo Z, A diferente.",
                    "Z número atômico, A número de massa, próton, nêutron, isótopo",
                    "1) compare Z; 2) se Z for igual, compare A; 3) Z igual e A diferente indica isótopos.",
                    "Comparar apenas o número de massa e concluir que são do mesmo elemento.",
                    "Carbono-12 e carbono-14 têm Z=6; diferem no número de nêutrons. São isótopos.",
                    "Veja primeiro o número de prótons."
            );
        }
        if(low.contains("mercantilismo")){
            return new Lesson(
                    "Mercantilismo é o conjunto de práticas econômicas associado aos Estados europeus modernos, com forte intervenção estatal e busca de acumulação de riqueza.",
                    "Associe mercantilismo a metalismo, balança comercial favorável, protecionismo, monopólios e exploração colonial — mas lembre que as práticas variaram entre países.",
                    "metalismo, protecionismo, monopólio, balança comercial, pacto colonial",
                    "1) identifique a prática descrita; 2) veja como ela fortalece o Estado/metrópole; 3) relacione-a ao comércio e à colonização.",
                    "Tratar mercantilismo como uma teoria única e idêntica em todos os lugares.",
                    "Uma metrópole que restringe o comércio da colônia e protege seus produtos com tarifas está usando práticas mercantilistas.",
                    "Pergunte: como essa medida favorece a metrópole?"
            );
        }
        if(low.contains("globalização")){
            return new Lesson(
                    "Globalização é a intensificação das conexões econômicas, produtivas, informacionais, culturais e financeiras entre lugares do mundo.",
                    "Integração não significa igualdade: fluxos globais conectam territórios de forma desigual.",
                    "fluxos, redes, transnacionais, comércio, tecnologia, divisão internacional do trabalho",
                    "1) identifique qual fluxo aparece; 2) observe os agentes envolvidos; 3) analise a escala; 4) procure consequências e desigualdades.",
                    "Concluir que globalização eliminou fronteiras ou tornou todos os lugares igualmente conectados.",
                    "Um celular pode ser projetado em um país, usar peças de vários outros e ser montado em outro: isso revela redes produtivas globais.",
                    "Descubra primeiro o que está circulando: mercadoria, capital, informação ou pessoas."
            );
        }
        return genericLesson(subject,topic);
    }

    private Lesson genericLesson(String subject,String topic){
        String what, remember, steps, trap, worked, micro;
        if(subject.equals("Português")){
            what="Este tópico ajuda a perceber como o texto constrói sentido. Em prova, a regra gramatical deve ser ligada ao trecho e à intenção do texto, não usada isoladamente.";
            remember="Leia a pergunta antes de caçar uma regra. Volte ao trecho, identifique a pista concreta e só então compare as alternativas.";
            steps="1) localize o trecho; 2) identifique a palavra/estrutura relevante; 3) explique o efeito no sentido; 4) elimine alternativas que exageram ou contradizem o texto.";
            trap="Responder pela memória de uma regra sem conferir o contexto do texto.";
            worked="Em uma questão contextualizada de "+topic+", a resposta correta é a que explica o fenômeno e continua compatível com o sentido do trecho.";
            micro="Ache no texto uma única pista relacionada a '"+topic+"' e diga o que ela faz.";
        } else if(subject.equals("Matemática")){
            what=""+topic+" é um recurso matemático que aparece dentro de situações-problema. A parte difícil costuma ser reconhecer qual modelo usar antes de fazer contas.";
            remember="Separe dados, pergunta e relação entre grandezas. Estime o resultado antes de calcular para perceber respostas absurdas.";
            steps="1) escreva o que é conhecido; 2) marque a incógnita; 3) escolha a relação/fórmula; 4) calcule em etapas; 5) confira unidade, sinal e tamanho do resultado.";
            trap="Começar a calcular com todos os números do enunciado sem decidir o que cada um representa.";
            worked="Para estudar "+topic+", transforme um enunciado em dados → relação → cálculo → resposta com unidade. Essa sequência reduz erros de distração.";
            micro="Escreva apenas os dados e o que a questão quer descobrir. Não faça a conta ainda.";
        } else if(subject.equals("Física")){
            what=topic+" descreve uma relação física entre grandezas observáveis. Em prova, contexto, unidades e interpretação são tão importantes quanto a fórmula.";
            remember="Liste cada grandeza com sua unidade e faça uma previsão qualitativa: se uma aumenta, o que deve acontecer com a outra?";
            steps="1) faça um esquema; 2) registre dados e unidades; 3) escolha a lei física; 4) isole a incógnita; 5) calcule; 6) interprete o valor.";
            trap="Substituir números antes de entender o fenômeno ou misturar unidades incompatíveis.";
            worked="Em uma situação sobre "+topic+", primeiro identifique as grandezas envolvidas e só depois escolha a equação. A conta vem por último.";
            micro="Liste duas grandezas que aparecem neste tópico e suas unidades.";
        } else if(subject.equals("Química")){
            what=topic+" faz parte do modelo usado para explicar composição e transformações da matéria.";
            remember="Diferencie sempre o que é observação macroscópica do que é explicação microscópica.";
            steps="1) identifique substâncias/partículas; 2) compare propriedades; 3) classifique o processo; 4) relacione evidência e modelo; 5) confira conservação da matéria quando houver reação.";
            trap="Confundir mudança de estado com formação de uma nova substância ou misturar conceitos de átomo, elemento e molécula.";
            worked="Ao resolver "+topic+", escreva primeiro 'o que mudou?' e 'surgiu substância nova?'. Essa distinção elimina várias alternativas.";
            micro="Diga se o tópico trata de partícula, substância, mistura ou transformação.";
        } else if(subject.equals("Biologia")){
            what=topic+" deve ser entendido como parte de um sistema biológico, não como uma palavra para decorar isoladamente.";
            remember="Relacione estrutura → função → consequência. Em ecologia, relacione organismo → interação → ambiente.";
            steps="1) identifique o nível biológico; 2) reconheça a função/processo; 3) ligue causa e efeito; 4) compare com as alternativas.";
            trap="Escolher uma alternativa com palavra científica correta, mas relação de causa e efeito errada.";
            worked="Em "+topic+", monte uma frase de causa e efeito: 'quando X acontece, Y muda porque...'.";
            micro="Responda em uma frase: qual é a função ou consequência principal ligada a este tópico?";
        } else if(subject.equals("História")){
            what=topic+" precisa ser localizado em tempo, espaço, agentes e interesses. A prova costuma cobrar relações, não só datas.";
            remember="Pergunte quem agiu, em qual contexto, com quais interesses e quais consequências apareceram.";
            steps="1) localize período e lugar; 2) identifique agentes; 3) reconheça interesses/conflitos; 4) relacione causa e consequência; 5) compare a fonte com o contexto.";
            trap="Analisar uma ação histórica com valores atuais sem considerar o contexto ou decorar um evento sem saber por que aconteceu.";
            worked="Para "+topic+", faça uma cadeia curta: contexto → ação dos agentes → consequência. Esse formato ajuda em questões com texto-fonte.";
            micro="Escreva: quem? onde? por quê? para o tópico '"+topic+"'.";
        } else {
            what=topic+" ajuda a explicar como sociedade e natureza organizam o espaço geográfico em diferentes escalas.";
            remember="Leia mapas, gráficos e textos procurando localização, escala, fluxo, distribuição e desigualdade.";
            steps="1) identifique o fenômeno; 2) localize a escala; 3) observe padrões espaciais; 4) relacione causas; 5) interprete consequências.";
            trap="Descrever um mapa ou gráfico sem explicar a relação espacial que ele mostra.";
            worked="Em uma questão de "+topic+", compare onde o fenômeno é mais/menos intenso e procure um fator que explique a diferença.";
            micro="Diga em qual escala este tópico pode aparecer: local, regional, nacional ou global.";
        }
        return new Lesson(what,remember,topic+", conceito, contexto, relação, evidência",steps,trap,worked,micro);
    }

    private Example exampleFor(TopicRef ref, Lesson l){
        String low=ref.topic.toLowerCase(Locale.ROOT);
        if(low.contains("domínio")||low.contains("contradomínio")||low.contains("imagem")){
            return new Example("Considere f(x)=2x, domínio D={1,2,3,4} e contradomínio CD={1,2,3,4,5,6,7,8}. Qual é a imagem?",
                    new String[]{"{1,2,3,4}","{2,4,6,8}","{1,2,3,4,5,6,7,8}","{2,3,4,5}"},1,
                    "Aplicando a regra a cada elemento do domínio obtemos 2, 4, 6 e 8; são as saídas que realmente aparecem.",
                    new String[]{"Esse é o domínio: os valores de entrada.","","Esse é o contradomínio inteiro; nem todos os seus elementos precisam aparecer como saída.","Esses valores não são o conjunto produzido por 2x para o domínio dado."});
        }
        if(low.contains("singular")){
            return new Example("Na frase 'A estudante dedicada resolveu o problema', qual elemento está no singular e determina a concordância principal?",
                    new String[]{"A expressão 'a estudante'","A palavra 'resolveram'","Um sujeito oculto plural","Nenhum; a frase está toda no plural"},0,
                    "'A estudante' refere-se a uma pessoa; artigo, substantivo, adjetivo e verbo aparecem no singular.",
                    new String[]{"","'Resolveram' nem aparece na frase e seria plural.","O sujeito está explícito: 'a estudante'.","A frase apresenta marcas claras de singular."});
        }
        if(low.contains("inferência")){
            return new Example("Texto: 'Quando Pedro chegou ao ponto, guardou o guarda-chuva ainda molhado e sacudiu os sapatos antes de entrar no ônibus.' O que é possível inferir?",
                    new String[]{"Choveu recentemente no local","Pedro comprou sapatos novos","O ônibus estava quebrado","Pedro odeia chuva"},0,
                    "Guarda-chuva molhado e sapatos que precisam ser sacudidos são pistas suficientes para concluir que houve chuva/água recentemente.",
                    new String[]{"","O texto não traz pista sobre compra.","Nada indica problema mecânico.","Preferência pessoal não pode ser inferida dessas pistas."});
        }
        if(low.contains("porcentagem")){
            return new Example("Uma inscrição de R$ 240 recebe desconto de 15%. Qual é o valor final?",
                    new String[]{"R$ 204","R$ 225","R$ 216","R$ 180"},0,
                    "15% de 240 é 36. Como é desconto, 240−36=204.",
                    new String[]{"","225 subtrai apenas 15 reais, confundindo percentual com valor absoluto.","216 corresponde a 10% de desconto.","180 corresponde a 25% de desconto."});
        }
        if(low.contains("regra de três inversa")){
            return new Example("Uma equipe faz certo serviço em 12 dias com 5 trabalhadores, mantendo produtividade igual. Se houver 10 trabalhadores, qual tempo é esperado?",
                    new String[]{"24 dias","12 dias","6 dias","2 dias"},2,
                    "Dobrar o número de trabalhadores reduz o tempo pela metade: são grandezas inversamente proporcionais.",
                    new String[]{"Isso trataria a relação como direta.","Ignora a mudança na equipe.","","Reduz o tempo muito mais do que a proporcionalidade permite."});
        }
        if(low.contains("lei de ohm")){
            return new Example("Um resistor de 6 Ω é submetido a 12 V. Admitindo comportamento ôhmico, qual corrente o atravessa?",
                    new String[]{"0,5 A","2 A","6 A","72 A"},1,
                    "Pela Lei de Ohm, I=U/R=12/6=2 A.",
                    new String[]{"É o inverso da divisão correta.","","6 é o valor da resistência, não da corrente.","72 resulta de multiplicar U por R em vez de dividir."});
        }
        if(low.contains("isótop")){
            return new Example("Dois átomos possuem o mesmo número atômico e números de massa diferentes. Como são classificados?",
                    new String[]{"Isótopos","Isóbaros","Isótonos","Moléculas"},0,
                    "Mesmo número atômico significa mesmo número de prótons; com massa diferente, são isótopos.",
                    new String[]{"","Isóbaros têm o mesmo número de massa.","Isótonos têm o mesmo número de nêutrons.","Átomos não se tornam moléculas apenas por essa relação."});
        }
        if(low.contains("mercantilismo")){
            return new Example("Uma monarquia aumenta tarifas sobre produtos estrangeiros e reserva parte do comércio colonial a comerciantes autorizados. Essas medidas se aproximam de qual prática?",
                    new String[]{"Mercantilismo","Feudalização","Socialismo industrial","Liberalismo de livre-comércio"},0,
                    "Protecionismo e controle/monopólio comercial são práticas associadas ao mercantilismo.",
                    new String[]{"","O caso descreve políticas comerciais de um Estado moderno, não retorno ao feudalismo.","O contexto e as medidas não correspondem ao socialismo industrial.","Tarifas e monopólios restringem, em vez de ampliar, o livre-comércio."});
        }
        if(low.contains("globalização")){
            return new Example("Uma empresa projeta um produto em um país, compra componentes em três continentes e o monta em outro para vender mundialmente. O caso evidencia principalmente:",
                    new String[]{"Redes produtivas globais","Fim do comércio internacional","Autossuficiência nacional","Desaparecimento das desigualdades"},0,
                    "A produção distribuída entre vários territórios mostra uma rede produtiva articulada em escala global.",
                    new String[]{"","O exemplo mostra justamente comércio e circulação internacionais.","Há forte interdependência, não autossuficiência.","Integração global não implica fim das desigualdades."});
        }
        String correct="A alternativa que aplica o conceito de "+ref.topic+" ao contexto sem contradizer os dados.";
        return new Example("Em uma questão contextualizada sobre "+ref.topic+", qual estratégia é mais segura antes de escolher a resposta?",
                new String[]{correct,"Escolher a alternativa com mais palavras técnicas","Usar todos os números do enunciado imediatamente","Ignorar texto, gráfico ou fonte e lembrar apenas uma definição"},0,
                "Questões fortes exigem ligar conceito, dados e contexto. A alternativa correta precisa explicar a situação, não apenas soar técnica.",
                new String[]{"","Vocabulário técnico não garante relação correta entre as ideias.","Nem todo número precisa entrar no cálculo; primeiro é necessário modelar o problema.","O contexto é parte da questão e não pode ser descartado."});
    }

    private Gloss gloss(String raw){
        String q=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);
        if(q.contains("singular"))return new Gloss("Singular","Forma usada quando se fala de uma unidade: uma pessoa, coisa, ideia ou elemento.","'O aluno estudou' está no singular; 'os alunos estudaram' está no plural.");
        if(q.contains("infer"))return new Gloss("Inferência","Conclusão obtida a partir de pistas do texto, mesmo quando a conclusão não aparece escrita literalmente.","Guarda-chuva molhado + rua com poças pode permitir inferir que choveu.");
        if(q.equals("domínio")||q.contains("dominio"))return new Gloss("Domínio","Conjunto de valores permitidos como entrada de uma função.","Em f(x)=2x com x∈{1,2,3}, o domínio é {1,2,3}.");
        if(q.contains("contradom"))return new Gloss("Contradomínio","Conjunto definido como possível destino das saídas de uma função; pode conter valores que a função não atingiu naquele domínio.","CD={1,...,8} pode conter a imagem {2,4,6,8}.");
        if(q.equals("imagem"))return new Gloss("Imagem","Conjunto das saídas que a função realmente assume ao aplicar a regra aos valores do domínio.","Se f(x)=2x em {1,2,3}, a imagem é {2,4,6}.");
        if(q.contains("tese"))return new Gloss("Tese","Ideia central defendida em um texto argumentativo.","Os argumentos são usados para sustentar a tese.");
        if(q.contains("coes"))return new Gloss("Coesão","Mecanismos que ligam palavras, frases e partes do texto, como pronomes, repetições controladas e conectivos.","'Porém' pode ligar ideias em contraste.");
        if(q.contains("propor"))return new Gloss("Proporção","Igualdade entre duas razões. É usada quando duas relações mantêm a mesma razão.","2/3=4/6 forma uma proporção.");
        if(q.contains("isótop")||q.contains("isotop"))return new Gloss("Isótopos","Átomos do mesmo elemento, com o mesmo número de prótons e diferente número de nêutrons/massa.","Carbono-12 e carbono-14.");
        if(q.contains("mercantil"))return new Gloss("Mercantilismo","Conjunto de práticas econômicas de Estados europeus modernos, incluindo protecionismo, monopólios e busca de acumulação de riqueza.","Restringir o comércio colonial em favor da metrópole.");
        if(q.contains("global"))return new Gloss("Globalização","Intensificação das conexões e fluxos entre lugares do mundo em redes econômicas, produtivas, tecnológicas e culturais.","Um produto com peças fabricadas em vários países.");
        for(String s:CURRICULUM.keySet())for(String t:CURRICULUM.get(s))if(t.toLowerCase(Locale.ROOT).contains(q)&&q.length()>2){Lesson l=lessonFor(s,t);return new Gloss(t,l.what,l.worked);}
        return new Gloss(raw.isEmpty()?"Termo não informado":raw,"Ainda não há uma definição específica para este termo no glossário. Procure o tópico relacionado na matéria; a mini-aula começa pela definição.","Tente pesquisar uma palavra mais curta ou o nome exato do tópico.");
    }

    @Override protected void onDestroy(){stopTicker();super.onDestroy();}

    private static class TopicRef {final String subject,topic;TopicRef(String s,String t){subject=s;topic=t;}}
    private static class Lesson {
        final String what,remember,words,steps,trap,worked,microStep;
        Lesson(String a,String b,String c,String d,String e,String f,String g){what=a;remember=b;words=c;steps=d;trap=e;worked=f;microStep=g;}
    }
    private static class Example {
        final String question;final String[] options;final int correct;final String correctWhy;final String[] wrongWhy;
        Example(String q,String[] o,int c,String cw,String[] ww){question=q;options=o;correct=c;correctWhy=cw;wrongWhy=ww;}
    }
    private static class Gloss {final String term,meaning,example;Gloss(String t,String m,String e){term=t;meaning=m;example=e;}}
}
