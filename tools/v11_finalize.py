from pathlib import Path

p = Path('cefet-trainer/app/src/main/java/com/cefettrainer/app/V10Activity.java')
s = p.read_text(encoding='utf-8')

# Version label in UI/comment.
s = s.replace('CEFET Trainer v0.10', 'CEFET Trainer v0.11')
s = s.replace('CEFET Trainer v0.10\n', 'CEFET Trainer v0.11\n')

# Make scrolling explicit and persistent so long subject/topic lists do not look truncated.
old_page = 'private ScrollView page(LinearLayout l){ ScrollView s=new ScrollView(this); s.setFillViewport(true); s.addView(l); return s; }'
new_page = '''private ScrollView page(LinearLayout l){
        ScrollView s=new ScrollView(this);
        s.setFillViewport(true);
        s.setVerticalScrollBarEnabled(true);
        s.setScrollbarFadingEnabled(false);
        s.setClipToPadding(false);
        s.setPadding(0,0,0,36);
        s.addView(l);
        return s;
    }'''
if old_page not in s:
    raise SystemExit('page() anchor not found')
s = s.replace(old_page, new_page)

# Home: show a real 0-100 learning percentage rather than only counts.
old_stats = '''int learned=countAtLeast(1), solid=countAtLeast(3), total=countTopics(), due=countDue();
        LinearLayout stats=card();
        stats.addView(tv("Seu mapa de estudo",19,true));
        stats.addView(tv(learned+" / "+total+" tópicos iniciados",16,false));
        stats.addView(tv(solid+" tópicos com domínio forte",16,false));
        stats.addView(tv(due+" revisões disponíveis agora",16,false));
        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pb.setMax(Math.max(1,total)); pb.setProgress(solid); stats.addView(pb,new LinearLayout.LayoutParams(-1,28));
        x.addView(stats);'''
new_stats = '''int learned=countAtLeast(1), solid=countAtLeast(3), total=countTopics(), due=countDue();
        int overall=overallPercent();
        LinearLayout stats=card();
        stats.addView(tv("Seu mapa de estudo — "+overall+"%",19,true));
        stats.addView(tv(learned+" / "+total+" tópicos iniciados",16,false));
        stats.addView(tv(solid+" tópicos com domínio forte",16,false));
        stats.addView(tv(due+" revisões disponíveis agora",16,false));
        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100); pb.setProgress(overall); stats.addView(pb,new LinearLayout.LayoutParams(-1,28));
        stats.addView(muted("0% = ainda não estudado. 35% = começou. 70% = entendeu. 100% = consegue recuperar e aplicar.",13));
        x.addView(stats);'''
if old_stats not in s:
    raise SystemExit('home stats anchor not found')
s = s.replace(old_stats, new_stats)

# Add foundation mode to home.
anchor_home_btn = 'x.addView(button("Plano de hoje — 3 blocos",GREEN,v->startDailyPlan()));'
if anchor_home_btn not in s:
    raise SystemExit('home button anchor not found')
s = s.replace(anchor_home_btn, 'x.addView(button("Reconstruir minha base — começar do zero",AMBER,v->foundationMode()));\n        '+anchor_home_btn)

# Subjects list: percentage per subject.
old_subject_btn = 'x.addView(button(s+"  •  "+done+"/"+total, BLUE, v->topicList(s)));'
new_subject_btn = 'x.addView(button(s+"  •  "+subjectPercent(s)+"%  •  "+done+"/"+total, BLUE, v->topicList(s)));'
if old_subject_btn not in s:
    raise SystemExit('subject button anchor not found')
s = s.replace(old_subject_btn, new_subject_btn)

# Topic list: subject overview, percentage, visible bottom cue.
old_topic_head = '''LinearLayout x=root(); title(x,subject);
        x.addView(muted("Legenda: ○ novo   ◔ iniciado   ● domínio forte   ↻ revisar",14));
        String[] arr=CURRICULUM.get(subject);'''
new_topic_head = '''LinearLayout x=root(); title(x,subject);
        LinearLayout overview=card();
        overview.addView(tv("Resumo da matéria — "+subjectPercent(subject)+"%",20,true));
        overview.addView(tv(subjectOverview(subject),16,false));
        ProgressBar sb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        sb.setMax(100); sb.setProgress(subjectPercent(subject)); overview.addView(sb,new LinearLayout.LayoutParams(-1,26));
        x.addView(overview);
        x.addView(muted("Legenda: ○ novo   ◔ iniciado   ● domínio forte   ↻ revisar. A porcentagem mostra seu domínio daquele tópico.",14));
        String[] arr=CURRICULUM.get(subject);'''
if old_topic_head not in s:
    raise SystemExit('topic head anchor not found')
s = s.replace(old_topic_head, new_topic_head)

old_topic_btn = 'x.addView(button(icon+"  "+t,color,v->lessonScreen(new TopicRef(subject,t))));'
new_topic_btn = 'x.addView(button(icon+"  "+t+"  •  "+topicPercent(subject,t)+"%",color,v->lessonScreen(new TopicRef(subject,t))));'
if old_topic_btn not in s:
    raise SystemExit('topic button anchor not found')
s = s.replace(old_topic_btn, new_topic_btn)

old_topic_end = '''        }
        setContentView(page(x));
    }

    private void lessonScreen(TopicRef ref){'''
new_topic_end = '''        }
        x.addView(muted("Fim da lista de "+subject+" ✓",14));
        Space bottom=new Space(this); x.addView(bottom,new LinearLayout.LayoutParams(1,72));
        setContentView(page(x));
    }

    private void lessonScreen(TopicRef ref){'''
if old_topic_end not in s:
    raise SystemExit('topic end anchor not found')
s = s.replace(old_topic_end, new_topic_end, 1)

# Replace the lesson screen with a simple-memory-first layout and inline instant-feedback question.
start = s.index('    private void lessonScreen(TopicRef ref){')
end = s.index('    private void simplify(TopicRef ref, Lesson l){', start)
new_lesson = r'''    private void lessonScreen(TopicRef ref){
        Lesson l=lessonFor(ref.subject,ref.topic);
        LinearLayout x=root(); title(x,ref.subject+" • "+ref.topic+" • "+topicPercent(ref.subject,ref.topic)+"%");

        LinearLayout summary=card();
        summary.addView(tv("RESUMO PARA MEMORIZAR",20,true));
        summary.addView(tv(memorySummary(ref,l),18,false));
        TextView pocket=tv("Frase de bolso: "+memoryPhrase(ref,l),17,true); pocket.setTextColor(BLUE); summary.addView(pocket);
        x.addView(summary);

        LinearLayout base=card();
        base.addView(tv("Antes disso, lembre desta base",18,true));
        base.addView(tv(prerequisite(ref),16,false));
        x.addView(base);

        LinearLayout c=card();
        c.addView(tv("Entenda em frases curtas",19,true));
        c.addView(tv(easyExplanation(ref,l),17,false));
        c.addView(tv("Palavras importantes",17,true));
        c.addView(tv(l.words,15,false));
        x.addView(c);

        // The example is intentionally placed in the middle of the lesson.
        addInlineExample(x,ref,l);

        LinearLayout steps=card();
        steps.addView(tv("Como fazer na prova",19,true));
        steps.addView(tv(easySteps(l.steps),16,false));
        steps.addView(tv("Cuidado com isto",17,true));
        steps.addView(tv(shorten(l.trap,180),15,false));
        x.addView(steps);

        LinearLayout solved=card();
        solved.addView(tv("Exemplo resolvido em uma frase",19,true));
        solved.addView(tv(shorten(l.worked,220),16,false));
        x.addView(solved);

        x.addView(button("Estou travado — mostrar só o mínimo",AMBER,v->simplify(ref,l)));
        x.addView(button("Treinar outra pergunta sem pontos",GREEN,v->lessonScreen(ref)));
        x.addView(button("Entendi — marcar avanço",BLUE,v->{markUnderstanding(ref,2);boostMastery(ref,1);schedule(ref,3);advanceAfterExample(ref);}));
        x.addView(button("Marcar para revisar depois",Color.rgb(105,111,118),v->{schedule(ref,1);Toast.makeText(this,"Revisão marcada.",Toast.LENGTH_SHORT).show();}));
        setContentView(page(x));
    }

    private void addInlineExample(LinearLayout x, TopicRef ref, Lesson l){
        Example e=exampleFor(ref,l);
        LinearLayout q=card();
        q.addView(tv("AGORA TESTE NO MEIO DA EXPLICAÇÃO",19,true));
        q.addView(muted("Não vale pontos. Toque em uma alternativa e a explicação aparece na mesma hora.",14));
        q.addView(tv(e.question,17,true));
        RadioGroup g=new RadioGroup(this); g.setOrientation(RadioGroup.VERTICAL);
        ArrayList<RadioButton> rbs=new ArrayList<>();
        for(int i=0;i<4;i++){
            RadioButton rb=new RadioButton(this);
            rb.setId(1700+i);
            rb.setText((char)('A'+i)+") "+e.options[i]);
            rb.setTextSize(sp(16));
            rb.setPadding(8,10,8,10);
            g.addView(rb); rbs.add(rb);
        }
        q.addView(g);
        LinearLayout feedback=card();
        feedback.setVisibility(View.GONE);
        q.addView(feedback);
        final boolean[] answered={false};
        g.setOnCheckedChangeListener((group,checkedId)->{
            if(answered[0] || checkedId==-1)return;
            answered[0]=true;
            int chosen=checkedId-1700;
            for(int i=0;i<rbs.size();i++){
                if(i==e.correct){rbs.get(i).setTextColor(GREEN);rbs.get(i).setTypeface(null,1);}
                else if(i==chosen){rbs.get(i).setTextColor(RED);rbs.get(i).setTypeface(null,1);}
                rbs.get(i).setEnabled(false);
            }
            feedback.setVisibility(View.VISIBLE);
            feedback.removeAllViews();
            if(chosen==e.correct){
                TextView ok=tv("✓ CERTO",20,true); ok.setTextColor(GREEN); feedback.addView(ok);
                feedback.addView(tv("Por que está certo: "+e.correctWhy,15,false));
                recordAttempt(ref,true);
            }else{
                TextView bad=tv("✗ INCORRETO",20,true); bad.setTextColor(RED); feedback.addView(bad);
                feedback.addView(tv("Por que esta alternativa está errada: "+e.wrongWhy[chosen],15,false));
                TextView good=tv("A correta é "+(char)('A'+e.correct)+". Por quê: "+e.correctWhy,15,true); good.setTextColor(GREEN); feedback.addView(good);
                recordAttempt(ref,false);
            }
            feedback.addView(muted("Errar aqui é parte do estudo. Este exemplo não tira pontos e não reduz sua porcentagem.",14));
        });
        x.addView(q);
    }

'''
s = s[:start] + new_lesson + s[end:]

# Insert progress, summaries and foundation helpers before progressHub.
anchor = '    private void progressHub(){'
if anchor not in s:
    raise SystemExit('progressHub anchor not found')
helpers = r'''    private int topicPercent(String subject,String topic){
        int m=mastery(subject,topic);
        if(m<=0)return 0;
        if(m==1)return 35;
        if(m==2)return 70;
        return 100;
    }

    private int subjectPercent(String subject){
        String[] arr=CURRICULUM.get(subject);
        if(arr==null||arr.length==0)return 0;
        int sum=0; for(String t:arr)sum+=topicPercent(subject,t);
        return Math.round(sum/(float)arr.length);
    }

    private int overallPercent(){
        int n=0,sum=0;
        for(String subject:CURRICULUM.keySet())for(String t:CURRICULUM.get(subject)){sum+=topicPercent(subject,t);n++;}
        return n==0?0:Math.round(sum/(float)n);
    }

    private String subjectOverview(String subject){
        if(subject.equals("Português"))return "Objetivo: entender textos e perceber como as palavras constroem sentido. Ordem simples: entender o texto → achar pistas → reconhecer a ideia → observar a gramática dentro da frase.";
        if(subject.equals("Matemática"))return "Objetivo: entender o problema antes da conta. Ordem simples: números → frações → razão e porcentagem → equações → funções → geometria. Sempre escreva o que a questão deu e o que quer descobrir.";
        if(subject.equals("Física"))return "Objetivo: ligar uma situação real a grandezas como velocidade, força, energia, calor e eletricidade. Primeiro entenda o que está acontecendo; a fórmula vem depois.";
        if(subject.equals("Química"))return "Objetivo: entender do que a matéria é feita e como ela muda. Comece por matéria, mistura, átomo e tabela periódica; depois avance para ligações, pH e reações.";
        if(subject.equals("Biologia"))return "Objetivo: ligar estrutura, função e consequência nos seres vivos. Comece por célula e DNA; depois ecologia, evolução, corpo humano e saúde.";
        if(subject.equals("História"))return "Objetivo: entender quem fez o quê, onde, por qual motivo e com qual consequência. Datas ajudam, mas relações de causa e efeito são mais importantes.";
        return "Objetivo: entender como sociedade e natureza organizam o espaço. Comece por mapas e localização; depois clima, relevo, população, cidades, campo, economia e globalização.";
    }

    private String memorySummary(TopicRef ref, Lesson l){
        String t=ref.topic.toLowerCase(Locale.ROOT);
        if(t.contains("singular"))return "1) Singular = uma unidade.\n2) Plural = duas ou mais.\n3) As palavras da frase precisam combinar em número.";
        if(t.contains("domínio")||t.contains("contradomínio")||t.contains("imagem"))return "1) Domínio = entradas.\n2) Contradomínio = saídas possíveis.\n3) Imagem = saídas que realmente aconteceram.";
        if(t.contains("inferência"))return "1) Leia as pistas.\n2) Junte as pistas.\n3) Conclua somente o que o texto permite.";
        if(t.contains("porcentagem"))return "1) % significa 'a cada 100'.\n2) Ache a parte do total.\n3) Depois veja se é aumento, desconto ou comparação.";
        if(t.contains("equação do 1"))return "1) Existe um valor desconhecido.\n2) Deixe esse valor sozinho.\n3) Faça a mesma operação nos dois lados.";
        if(t.contains("função"))return "1) Entra um valor.\n2) A regra transforma esse valor.\n3) Sai outro valor.";
        if(t.contains("fraç"))return "1) Número de cima = partes escolhidas.\n2) Número de baixo = total de partes iguais.\n3) Fração é uma divisão.";
        if(ref.subject.equals("Português"))return "1) Entenda a frase ou texto.\n2) Ache a pista principal.\n3) Só depois escolha a regra de língua portuguesa.";
        if(ref.subject.equals("Matemática"))return "1) O que eu sei?\n2) O que preciso descobrir?\n3) Qual conta liga essas duas coisas?";
        if(ref.subject.equals("História"))return "1) Quem?\n2) Por quê?\n3) O que aconteceu depois?";
        if(ref.subject.equals("Geografia"))return "1) Onde acontece?\n2) Por que acontece ali?\n3) Qual consequência aparece no espaço?";
        if(ref.subject.equals("Física"))return "1) O que está acontecendo?\n2) Quais grandezas mudam?\n3) Qual relação física explica isso?";
        if(ref.subject.equals("Química"))return "1) O que existe antes?\n2) O que mudou?\n3) Surgiu uma substância nova ou só mudou o estado/forma?";
        return "1) Qual estrutura/processo aparece?\n2) Qual função ele tem?\n3) O que acontece se ele mudar?";
    }

    private String memoryPhrase(TopicRef ref, Lesson l){
        String t=ref.topic.toLowerCase(Locale.ROOT);
        if(t.contains("singular"))return "singular é UM; plural é MAIS DE UM.";
        if(t.contains("domínio")||t.contains("contradomínio")||t.contains("imagem"))return "domínio entra; imagem sai de verdade.";
        if(t.contains("inferência"))return "inferir é descobrir pelo que o texto deixa como pista.";
        if(t.contains("porcentagem"))return "porcentagem é uma parte de cada 100.";
        if(t.contains("função"))return "função recebe, transforma e devolve.";
        if(t.contains("equação"))return "equação é uma balança: o que faço de um lado, faço do outro.";
        if(ref.subject.equals("História"))return "contexto → ação → consequência.";
        if(ref.subject.equals("Geografia"))return "localize → compare → explique.";
        if(ref.subject.equals("Matemática"))return "dados → relação → conta → resposta.";
        if(ref.subject.equals("Português"))return "texto primeiro; regra depois.";
        return "entenda a ideia antes de decorar o nome.";
    }

    private String prerequisite(TopicRef ref){
        String t=ref.topic.toLowerCase(Locale.ROOT);
        if(t.contains("função"))return "Base: números, operações, expressão algébrica e equação simples. Se isso estiver esquecido, não pule: volte nesses tópicos primeiro.";
        if(t.contains("porcent")||t.contains("propor")||t.contains("regra de três"))return "Base: multiplicação, divisão, fração e número decimal.";
        if(t.contains("equação"))return "Base: operações com números e ideia de igualdade.";
        if(t.contains("infer")||t.contains("tese")||t.contains("coes"))return "Base: ler uma frase de cada vez, localizar informação explícita e saber quem/que coisa cada palavra retoma.";
        if(ref.subject.equals("Português"))return "Base: singular/plural, verbo, substantivo, sujeito e leitura literal da frase.";
        if(ref.subject.equals("Matemática"))return "Base: quatro operações, números negativos, frações e decimais.";
        if(ref.subject.equals("Física"))return "Base: operações, unidades e leitura de gráficos simples.";
        if(ref.subject.equals("Química"))return "Base: matéria, substância, mistura e átomo.";
        if(ref.subject.equals("Biologia"))return "Base: célula, organismo, ambiente e ideia de função.";
        if(ref.subject.equals("História"))return "Base: localizar antes/depois, lugar, grupo social e causa/consequência.";
        return "Base: mapa, direção, escala e diferença entre lugar, região e território.";
    }

    private String easyExplanation(TopicRef ref, Lesson l){
        return shorten(l.what,210)+"\n\nGuarde isto: "+shorten(l.remember,180);
    }

    private String easySteps(String raw){
        if(raw==null)return "Leia devagar. Separe dados e pergunta. Resolva uma etapa por vez.";
        String r=raw.replace("; ",".\n").replace(";",".\n");
        return shorten(r,360);
    }

    private String shorten(String raw,int max){
        if(raw==null)return "";
        String r=raw.trim().replaceAll("\\s+"," ");
        if(r.length()<=max)return r;
        int cut=r.lastIndexOf(' ',max);
        if(cut<max/2)cut=max;
        return r.substring(0,cut).trim()+"…";
    }

    private void foundationMode(){
        LinearLayout x=root(); title(x,"Reconstruir minha base");
        LinearLayout intro=card();
        intro.addView(tv("Começar do zero não apaga seu potencial",20,true));
        intro.addView(tv("Aqui o app não presume que você lembra do 6º, 7º ou 8º ano. A sequência começa pelas peças pequenas que sustentam o conteúdo do CEFET.",16,false));
        intro.addView(muted("Faça uma base por vez. Errar os exemplos não tira pontos.",14));
        x.addView(intro);
        addFoundationGroup(x,"Português — base de leitura",new String[]{"Singular e plural","Classes gramaticais","Período simples","Compreensão global do texto","Localização de informação explícita","Inferência"});
        addFoundationGroup(x,"Matemática — base numérica",new String[]{"Números naturais","Números inteiros","Frações","Números decimais","Razão","Proporção","Porcentagem","Equação do 1º grau","Função: domínio, contradomínio e imagem"});
        addFoundationGroup(x,"Ciências — base",new String[]{"Grandezas e unidades","Matéria e energia","Átomo","Célula procarionte e eucarionte","Ecossistema"});
        addFoundationGroup(x,"Humanas — base",new String[]{"Formação do mundo moderno","Expansão marítima europeia","Orientação e localização","Leitura de mapas","População absoluta e relativa"});
        x.addView(muted("Fim do roteiro de base ✓",14));
        setContentView(page(x));
    }

    private void addFoundationGroup(LinearLayout x,String titleText,String[] topics){
        x.addView(tv(titleText,19,true));
        for(String topic:topics){
            TopicRef found=findTopic(topic);
            if(found!=null)x.addView(button(topic+" • "+topicPercent(found.subject,found.topic)+"%",BLUE,v->lessonScreen(found)));
        }
    }

    private TopicRef findTopic(String topic){
        for(String subject:CURRICULUM.keySet())for(String t:CURRICULUM.get(subject))if(t.equals(topic))return new TopicRef(subject,t);
        return null;
    }

'''
s = s.replace(anchor, helpers + anchor, 1)

# Improve detailed progress view with percentages per subject.
old_progress_line = 'for(String s:CURRICULUM.keySet()){int st=countSubjectAtLeast(s,1), so=countSubjectAtLeast(s,3);x.addView(tv(s+": "+st+" iniciados • "+so+" fortes • "+CURRICULUM.get(s).length+" total",15,false));}'
new_progress_line = 'for(String s:CURRICULUM.keySet()){int st=countSubjectAtLeast(s,1), so=countSubjectAtLeast(s,3);x.addView(tv(s+": "+subjectPercent(s)+"% • "+st+" iniciados • "+so+" fortes • "+CURRICULUM.get(s).length+" total",15,false));}'
if old_progress_line not in s:
    raise SystemExit('progress line anchor not found')
s = s.replace(old_progress_line,new_progress_line)

p.write_text(s,encoding='utf-8')
print('v0.11 learning UX finalization OK')
