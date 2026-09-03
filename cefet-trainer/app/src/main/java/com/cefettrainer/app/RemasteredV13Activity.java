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

/**
 * CEFET Trainer 0.13.
 *
 * Esta Activity não reutiliza o fluxo de aula da 0.12. Cada tópico mostra posição
 * real na trilha, anterior/próximo reais, conteúdo completo visível e uma organização
 * pedagógica diferente por matéria. O botão de próximo nunca chama o recomendador global.
 */
public class RemasteredV13Activity extends Activity {
    private final int NAVY = Color.rgb(14, 38, 63);
    private final int BG = Color.rgb(244, 247, 250);
    private final int WHITE = Color.WHITE;
    private final int TEXT = Color.rgb(27, 36, 46);
    private final int MUTED = Color.rgb(84, 95, 106);
    private final int GREEN = Color.rgb(29, 126, 88);
    private final int RED = Color.rgb(184, 54, 54);
    private final int AMBER = Color.rgb(181, 112, 13);
    private final int BLUE = Color.rgb(39, 91, 160);
    private final int PURPLE = Color.rgb(106, 72, 153);
    private final int SOFT = Color.rgb(232, 237, 243);

    private LinkedHashMap<String, StudyCatalog.Node> nodes;
    private LinkedHashMap<String, ArrayList<StudyCatalog.Node>> bySubject;
    private SharedPreferences prefs;
    private float fontBoost;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        prefs = getSharedPreferences("cefet_v13_real", MODE_PRIVATE);
        fontBoost = prefs.getBoolean("large_font", false) ? 2f : 0f;
        nodes = StudyCatalog.create();
        indexSubjects();
        home();
    }

    private void indexSubjects() {
        bySubject = new LinkedHashMap<>();
        for (String subject : StudyCatalog.SUBJECT_ORDER) bySubject.put(subject, new ArrayList<>());
        for (StudyCatalog.Node n : nodes.values()) {
            if (!bySubject.containsKey(n.subject)) bySubject.put(n.subject, new ArrayList<>());
            bySubject.get(n.subject).add(n);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int sp(int base) {
        return Math.round(base + fontBoost);
    }

    private TextView tv(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value == null ? "" : value);
        t.setTextSize(sp(size));
        t.setTextColor(TEXT);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        t.setLineSpacing(dp(2), 1.09f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private TextView muted(String value, int size) {
        TextView t = tv(value, size, false);
        t.setTextColor(MUTED);
        return t;
    }

    private Button btn(String label, int color, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(sp(15));
        b.setTextColor(WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setPadding(dp(14), dp(8), dp(14), dp(8));
        b.setMinHeight(dp(50));
        b.setOnClickListener(click);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(5), 0, dp(5));
        b.setLayoutParams(lp);
        return b;
    }

    private Button smallBtn(String label, int color, View.OnClickListener click) {
        Button b = btn(label, color, click);
        b.setTextSize(sp(13));
        b.setMinHeight(dp(42));
        return b;
    }

    private LinearLayout root() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setBackgroundColor(BG);
        r.setPadding(dp(15), dp(15), dp(15), dp(100));
        return r;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackgroundColor(WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(7), 0, dp(7));
        c.setLayoutParams(lp);
        return c;
    }

    private LinearLayout tintedCard(int color) {
        LinearLayout c = card();
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        c.setBackgroundColor(Color.rgb((r + 255 * 5) / 6, (g + 255 * 5) / 6, (b + 255 * 5) / 6));
        return c;
    }

    private ScrollView page(LinearLayout content) {
        Space bottom = new Space(this);
        content.addView(bottom, new LinearLayout.LayoutParams(1, dp(100)));
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setVerticalScrollBarEnabled(true);
        s.setScrollbarFadingEnabled(false);
        s.addView(content, new ScrollView.LayoutParams(-1, -2));
        return s;
    }

    private ProgressBar progress(int pct) {
        ProgressBar p = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        p.setMax(100);
        p.setProgress(Math.max(0, Math.min(100, pct)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(18));
        lp.setMargins(dp(8), dp(2), dp(8), dp(5));
        p.setLayoutParams(lp);
        return p;
    }

    private void backHeader(LinearLayout x, String label, Runnable back) {
        x.addView(smallBtn("← Voltar", NAVY, v -> back.run()));
        TextView crumb = muted(label, 12);
        crumb.setTextColor(BLUE);
        x.addView(crumb);
    }

    private int subjectColor(String subject) {
        switch (subject) {
            case "Português": return Color.rgb(62, 91, 158);
            case "Matemática": return Color.rgb(20, 119, 123);
            case "Física": return Color.rgb(75, 86, 168);
            case "Química": return Color.rgb(116, 79, 150);
            case "Biologia": return Color.rgb(41, 126, 73);
            case "História": return Color.rgb(148, 88, 43);
            case "Geografia": return Color.rgb(39, 111, 142);
            default: return BLUE;
        }
    }

    private String[] teachingLabels(String subject) {
        switch (subject) {
            case "Português":
                return new String[]{"Leia a ideia sem decorar nome", "Veja funcionando na língua", "O que a prova tenta confundir", "Agora interprete sozinho"};
            case "Matemática":
                return new String[]{"Entenda antes da conta", "Resolva sem pular etapa", "Onde o raciocínio costuma quebrar", "Agora faça a transferência"};
            case "Física":
                return new String[]{"Fenômeno primeiro", "Grandezas e relação", "Erro físico comum", "Use o modelo em outra situação"};
            case "Química":
                return new String[]{"O que observamos", "O modelo por trás do fenômeno", "Não confunda estes conceitos", "Aplique em outra situação"};
            case "Biologia":
                return new String[]{"Estrutura e ideia central", "Função e consequência", "Confusão biológica comum", "Conecte com outro contexto"};
            case "História":
                return new String[]{"Contexto antes da data", "Agentes, causas e evidências", "Anacronismo ou simplificação a evitar", "Interprete uma situação nova"};
            case "Geografia":
                return new String[]{"Onde e em qual escala", "Relação espacial explicada", "Generalização que engana", "Aplique em outro espaço"};
            default:
                return new String[]{"Ideia central", "Exemplo", "Erro comum", "Aplicação"};
        }
    }

    private void home() {
        LinearLayout x = root();
        x.addView(tv("CEFET Trainer 0.13", 30, true));
        x.addView(muted("Agora a trilha realmente anda: cada aula tem posição, anterior, próximo e conteúdo próprio.", 14));

        StudyCatalog.Node next = recommendNext();
        if (next != null) {
            int color = subjectColor(next.subject);
            LinearLayout c = tintedCard(color);
            c.addView(tv("Continue daqui", 18, true));
            c.addView(tv(next.subject + " • " + next.title, 17, true));
            c.addView(muted(next.summary, 14));
            c.addView(smallBtn("Abrir aula", color, v -> lesson(next)));
            x.addView(c);
        }

        LinearLayout route = card();
        route.addView(tv("Escolha como estudar", 19, true));
        route.addView(muted("Base = sequência curta e acumulativa. Matéria = todos os tópicos daquela disciplina. Busca = vá direto ao conceito que travou.", 13));
        x.addView(route);
        x.addView(btn("Reconstruir minha base", GREEN, v -> foundation()));
        x.addView(btn("Aprender por matéria", BLUE, v -> subjectList()));
        x.addView(btn("Buscar um tópico", PURPLE, v -> searchScreen()));
        x.addView(btn("Revisões para fazer", AMBER, v -> reviewHub()));
        x.addView(btn("Simulado — 30 questões", NAVY, v -> startActivity(new Intent(this, RemasteredExamActivity.class))));
        x.addView(btn("Redação — foto/OCR + treino", Color.rgb(120, 71, 139), v -> startActivity(new Intent(this, EssayCoachActivity.class))));
        x.addView(btn("Meu progresso", Color.rgb(78, 91, 102), v -> progressScreen()));

        LinearLayout stats = card();
        stats.addView(tv("Visão rápida", 18, true));
        addMetric(stats, "Cobertura", coveragePercent(null));
        addMetric(stats, "Domínio", masteryPercent(null));
        addMetric(stats, "Precisão", accuracyPercent());
        x.addView(stats);

        x.addView(smallBtn(prefs.getBoolean("large_font", false) ? "Fonte maior: ligada" : "Fonte maior: desligada", Color.rgb(102, 108, 116), v -> {
            boolean on = !prefs.getBoolean("large_font", false);
            prefs.edit().putBoolean("large_font", on).apply();
            fontBoost = on ? 2f : 0f;
            home();
        }));
        setContentView(page(x));
    }

    private void addMetric(LinearLayout box, String title, int pct) {
        box.addView(tv(title + " • " + pct + "%", 14, true));
        box.addView(progress(pct));
    }

    private void subjectList() {
        LinearLayout x = root();
        backHeader(x, "Início > matérias", this::home);
        x.addView(tv("Matérias", 27, true));
        x.addView(muted("Não são botões genéricos: cada matéria abre um mapa numerado dos tópicos e mostra o que cada aula realmente ensina.", 14));
        for (String subject : StudyCatalog.SUBJECT_ORDER) {
            ArrayList<StudyCatalog.Node> list = bySubject.get(subject);
            int count = list == null ? 0 : list.size();
            int color = subjectColor(subject);
            LinearLayout c = tintedCard(color);
            c.addView(tv(subject, 20, true));
            c.addView(muted(StudyCatalog.subjectIntro(subject), 13));
            c.addView(tv(count + " aulas • " + coveragePercent(subject) + "% abertas • " + masteryPercent(subject) + "% domínio", 13, true));
            c.addView(smallBtn("Ver mapa de " + subject, color, v -> subjectScreen(subject)));
            x.addView(c);
        }
        setContentView(page(x));
    }

    private void subjectScreen(String subject) {
        LinearLayout x = root();
        backHeader(x, "Início > matérias > " + subject, this::subjectList);
        int color = subjectColor(subject);
        x.addView(tv(subject, 28, true));
        ArrayList<StudyCatalog.Node> list = bySubject.get(subject);
        if (list == null || list.isEmpty()) {
            x.addView(muted("Ainda não existem aulas nesta matéria.", 14));
            setContentView(page(x));
            return;
        }

        LinearLayout map = tintedCard(color);
        map.addView(tv("Mapa da matéria", 18, true));
        map.addView(muted("A ordem abaixo é a sequência real desta versão. O botão de próximo usa exatamente esta lista.", 13));
        map.addView(tv("Cobertura " + coveragePercent(subject) + "% • domínio " + masteryPercent(subject) + "%", 14, true));
        map.addView(progress(readinessPercent(subject)));
        x.addView(map);

        int lastLevel = -1;
        for (int i = 0; i < list.size(); i++) {
            StudyCatalog.Node n = list.get(i);
            if (n.level != lastLevel) {
                lastLevel = n.level;
                x.addView(tv(levelName(lastLevel), 18, true));
            }
            int pos = i + 1;
            LinearLayout c = card();
            c.addView(tv(String.format(Locale.getDefault(), "%02d. %s", pos, n.title), 17, true));
            c.addView(muted(n.summary, 13));
            String status = prefs.getBoolean("seen::" + n.id, false) ? nodePercent(n.id) + "% no tópico" : "ainda não aberto";
            c.addView(muted(status, 12));
            c.addView(smallBtn("Abrir tópico " + pos + " de " + list.size(), color, v -> lesson(n)));
            x.addView(c);
        }
        setContentView(page(x));
    }

    private String levelName(int level) {
        if (level <= 1) return "Fundamentos";
        if (level == 2) return "Construção e aplicação";
        return "Raciocínio mais avançado";
    }

    private void foundation() {
        LinearLayout x = root();
        backHeader(x, "Início > reconstruir base", this::home);
        x.addView(tv("Reconstruir minha base", 27, true));
        x.addView(muted("Aqui o próximo passo segue a lista abaixo, não um recomendador que pode devolver a mesma aula.", 14));

        int total = 0;
        for (String id : StudyCatalog.FOUNDATION) if (nodes.containsKey(id)) total++;
        int pos = 0;
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node n = nodes.get(id);
            if (n == null) continue;
            pos++;
            int currentPos = pos;
            int color = subjectColor(n.subject);
            LinearLayout c = tintedCard(color);
            c.addView(tv(String.format(Locale.getDefault(), "%02d/%02d • %s", currentPos, total, n.title), 17, true));
            c.addView(muted(n.subject + " • " + n.summary, 13));
            c.addView(smallBtn("Estudar esta etapa", color, v -> lessonFoundation(n)));
            x.addView(c);
        }
        setContentView(page(x));
    }

    private void lessonFoundation(StudyCatalog.Node n) {
        lessonInternal(n, true);
    }

    private void lesson(StudyCatalog.Node n) {
        lessonInternal(n, false);
    }

    private void lessonInternal(StudyCatalog.Node n, boolean foundationFlow) {
        markSeen(n.id);
        int color = subjectColor(n.subject);
        LinearLayout x = root();
        Runnable back = foundationFlow ? this::foundation : () -> subjectScreen(n.subject);
        backHeader(x, foundationFlow ? "Base > " + n.title : n.subject + " > " + n.title, back);

        int subjectIndex = indexInSubject(n);
        int subjectTotal = bySubject.containsKey(n.subject) ? bySubject.get(n.subject).size() : 1;
        int foundationIndex = indexInFoundation(n.id);
        int foundationTotal = foundationCount();
        String position = foundationFlow && foundationIndex >= 0
                ? "Etapa " + (foundationIndex + 1) + " de " + foundationTotal + " da base"
                : "Tópico " + (subjectIndex + 1) + " de " + subjectTotal + " em " + n.subject;

        LinearLayout hero = tintedCard(color);
        hero.addView(tv(position, 13, true));
        hero.addView(tv(n.title, 26, true));
        hero.addView(muted("Nível " + n.level + " • progresso neste tópico: " + nodePercent(n.id) + "%", 13));
        hero.addView(progress(nodePercent(n.id)));
        x.addView(hero);

        addRoadmap(x, n, foundationFlow, color);
        addPrerequisites(x, n, foundationFlow, color);

        String[] labels = teachingLabels(n.subject);

        LinearLayout objective = card();
        objective.addView(tv("Objetivo desta aula", 18, true));
        objective.addView(tv("Ao terminar, você deve conseguir explicar e reconhecer: " + n.title + ".", 15, false));
        objective.addView(muted(n.summary, 14));
        x.addView(objective);

        LinearLayout block1 = tintedCard(color);
        block1.addView(tv(labels[0], 19, true));
        block1.addView(tv(n.memory, 17, true));
        block1.addView(tv(n.summary, 15, false));
        x.addView(block1);

        LinearLayout terms = card();
        terms.addView(tv("Vocabulário necessário", 17, true));
        terms.addView(tv(n.terms, 14, false));
        terms.addView(smallBtn("Não entendi uma palavra", PURPLE, v -> glossaryDialog()));
        x.addView(terms);

        LinearLayout block2 = card();
        block2.addView(tv(labels[1], 19, true));
        block2.addView(tv(n.worked, 15, false));
        block2.addView(tv("Explicação completa", 16, true));
        block2.addView(tv(n.detail, 15, false));
        x.addView(block2);

        LinearLayout check = tintedCard(Color.rgb(75, 113, 161));
        check.addView(tv("Cheque se a ideia entrou", 18, true));
        check.addView(muted("Responda antes de continuar. O feedback explica sua alternativa imediatamente.", 13));
        renderQuestion(check, n, n.check, "check", foundationFlow);
        x.addView(check);

        LinearLayout misconception = card();
        misconception.addView(tv(labels[2], 18, true));
        misconception.addView(tv(n.counterExample, 14, false));
        misconception.addView(tv("Erro recorrente", 16, true));
        misconception.addView(tv(n.trap, 14, false));
        x.addView(misconception);

        LinearLayout transfer = tintedCard(Color.rgb(55, 128, 94));
        transfer.addView(tv(labels[3], 18, true));
        transfer.addView(muted("A pergunta muda o contexto. Se você só decorou o exemplo, aqui isso aparece.", 13));
        renderQuestion(transfer, n, n.transfer, "transfer", foundationFlow);
        x.addView(transfer);

        x.addView(btn("Marcar este tópico para revisar amanhã", AMBER, v -> {
            schedule(n.id, 1);
            Toast.makeText(this, "Revisão marcada para amanhã.", Toast.LENGTH_SHORT).show();
        }));

        addNavigation(x, n, foundationFlow, color);
        setContentView(page(x));
    }

    private void addRoadmap(LinearLayout x, StudyCatalog.Node n, boolean foundationFlow, int color) {
        StudyCatalog.Node previous = foundationFlow ? previousFoundation(n) : previousInSubject(n);
        StudyCatalog.Node next = foundationFlow ? nextFoundation(n) : nextInSubject(n);
        LinearLayout road = card();
        road.addView(tv("Onde você está na sequência", 17, true));
        road.addView(muted("Anterior: " + (previous == null ? "início da trilha" : previous.title), 13));
        TextView current = tv("AGORA: " + n.title, 15, true);
        current.setTextColor(color);
        road.addView(current);
        road.addView(muted("Depois: " + (next == null ? "fim desta trilha" : next.title), 13));
        x.addView(road);
    }

    private void addPrerequisites(LinearLayout x, StudyCatalog.Node n, boolean foundationFlow, int color) {
        if (n.prerequisites == null || n.prerequisites.length == 0) return;
        LinearLayout pre = card();
        pre.addView(tv("Base necessária para esta aula", 17, true));
        boolean missing = false;
        for (String id : n.prerequisites) {
            StudyCatalog.Node p = nodes.get(id);
            if (p == null) continue;
            int m = nodeMastery(id);
            if (m < 2) missing = true;
            pre.addView(smallBtn((m >= 2 ? "✓ " : "⚠ ") + p.title, m >= 2 ? GREEN : AMBER,
                    v -> lessonInternal(p, foundationFlow)));
        }
        pre.addView(muted(missing ? "Há pelo menos um pré-requisito ainda fraco. Você pode voltar nele sem perder esta sequência." : "Os pré-requisitos principais já foram praticados.", 12));
        x.addView(pre);
    }

    private void addNavigation(LinearLayout x, StudyCatalog.Node n, boolean foundationFlow, int color) {
        StudyCatalog.Node previous = foundationFlow ? previousFoundation(n) : previousInSubject(n);
        StudyCatalog.Node next = foundationFlow ? nextFoundation(n) : nextInSubject(n);
        LinearLayout nav = card();
        nav.addView(tv("Continuar a trilha", 18, true));
        if (previous != null) {
            nav.addView(btn("← Anterior: " + previous.title, Color.rgb(91, 101, 111), v -> lessonInternal(previous, foundationFlow)));
        }
        if (next != null) {
            nav.addView(btn("Próximo: " + next.title + " →", color, v -> lessonInternal(next, foundationFlow)));
        } else {
            nav.addView(btn(foundationFlow ? "Base concluída — voltar ao início" : "Fim da matéria — voltar ao mapa", GREEN,
                    v -> { if (foundationFlow) home(); else subjectScreen(n.subject); }));
        }

        if (!foundationFlow && indexInFoundation(n.id) >= 0) {
            StudyCatalog.Node baseNext = nextFoundation(n);
            if (baseNext != null && (next == null || !baseNext.id.equals(next.id))) {
                nav.addView(smallBtn("Seguir a trilha geral da base: " + baseNext.title, AMBER, v -> lessonFoundation(baseNext)));
            }
        }
        x.addView(nav);
    }

    private void renderQuestion(LinearLayout box, StudyCatalog.Node n, StudyCatalog.Question q, String phase, boolean foundationFlow) {
        if (q == null) return;
        box.addView(tv(q.prompt, 16, true));
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        ArrayList<RadioButton> radios = new ArrayList<>();
        HashMap<Integer, Integer> indexById = new HashMap<>();

        for (int i = 0; i < q.options.length; i++) {
            RadioButton rb = new RadioButton(this);
            int id = View.generateViewId();
            rb.setId(id);
            indexById.put(id, i);
            rb.setText((char)('A' + i) + ") " + q.options[i]);
            rb.setTextSize(sp(15));
            rb.setTextColor(TEXT);
            rb.setPadding(dp(6), dp(7), dp(6), dp(7));
            radios.add(rb);
            group.addView(rb);
        }
        box.addView(group);

        LinearLayout feedback = card();
        feedback.setBackgroundColor(SOFT);
        feedback.setVisibility(View.GONE);
        box.addView(feedback);
        final boolean[] answered = {false};

        group.setOnCheckedChangeListener((g, checkedId) -> {
            if (answered[0]) return;
            Integer chosenObj = indexById.get(checkedId);
            if (chosenObj == null) return;
            answered[0] = true;
            int chosen = chosenObj;
            boolean correct = chosen == q.correct;
            recordAttempt(n.id, correct, phase);

            for (int i = 0; i < radios.size(); i++) {
                RadioButton rb = radios.get(i);
                rb.setEnabled(false);
                if (i == q.correct) {
                    rb.setTextColor(GREEN);
                    rb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                } else if (i == chosen) {
                    rb.setTextColor(RED);
                    rb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                }
            }

            feedback.setVisibility(View.VISIBLE);
            TextView result = tv(correct ? "✓ Acertou" : "✗ Ainda há uma lacuna aqui", 18, true);
            result.setTextColor(correct ? GREEN : RED);
            feedback.addView(result);
            feedback.addView(tv("Sua escolha: " + q.feedback[chosen], 14, false));
            if (!correct) {
                TextView right = tv("Resposta correta: " + q.options[q.correct] + " — " + q.feedback[q.correct], 14, true);
                right.setTextColor(GREEN);
                feedback.addView(right);
            }
            feedback.addView(tv("Entenda todas as alternativas", 15, true));
            for (int i = 0; i < q.options.length; i++) {
                TextView why = muted(((i == q.correct) ? "✓ " : "• ") + (char)('A' + i) + ": " + q.feedback[i], 12);
                if (i == q.correct) why.setTextColor(GREEN);
                feedback.addView(why);
            }

            String route = chosen < q.route.length ? q.route[chosen] : null;
            if (!correct && route != null && nodes.containsKey(route)) {
                StudyCatalog.Node repair = nodes.get(route);
                feedback.addView(tv("O erro aponta para uma base anterior", 15, true));
                feedback.addView(muted(repair.title, 13));
                feedback.addView(smallBtn("Reparar " + repair.title + " agora", AMBER,
                        v -> lessonInternal(repair, foundationFlow)));
            }
        });
    }

    private void glossaryDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(6), dp(12), dp(6));
        EditText input = new EditText(this);
        input.setHint("Ex.: singular, sujeito, razão, domínio...");
        input.setSingleLine(true);
        box.addView(input);
        TextView answer = tv("Digite uma palavra.", 14, false);
        box.addView(answer);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Dicionário rápido")
                .setView(box)
                .setPositiveButton("Procurar", null)
                .setNegativeButton("Fechar", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String q = normalize(input.getText().toString());
            if (q.isEmpty()) {
                answer.setText("Digite uma palavra.");
                return;
            }
            String foundKey = null;
            String foundValue = null;
            for (Map.Entry<String, String> e : StudyCatalog.glossary().entrySet()) {
                String key = normalize(e.getKey());
                if (key.contains(q) || q.contains(key)) {
                    foundKey = e.getKey();
                    foundValue = e.getValue();
                    break;
                }
            }
            answer.setText(foundValue == null ? "Ainda não há verbete específico para essa palavra." : foundKey.toUpperCase(Locale.ROOT) + "\n\n" + foundValue);
        }));
        dialog.show();
    }

    private void searchScreen() {
        LinearLayout x = root();
        backHeader(x, "Início > busca", this::home);
        x.addView(tv("Buscar tópico", 27, true));
        EditText search = new EditText(this);
        search.setHint("Ex.: equação, substantivo, fração, átomo, clima...");
        search.setSingleLine(true);
        x.addView(search);
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        x.addView(results);

        Runnable refresh = () -> renderSearch(results, search.getText().toString());
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { refresh.run(); }
            public void afterTextChanged(Editable s) {}
        });
        renderSearch(results, "");
        setContentView(page(x));
    }

    private void renderSearch(LinearLayout results, String raw) {
        results.removeAllViews();
        String q = normalize(raw);
        int shown = 0;
        for (StudyCatalog.Node n : nodes.values()) {
            String hay = normalize(n.subject + " " + n.title + " " + n.summary + " " + n.terms + " " + n.detail);
            if (q.isEmpty() || hay.contains(q)) {
                int color = subjectColor(n.subject);
                LinearLayout c = card();
                c.addView(tv(n.subject + " • " + n.title, 16, true));
                c.addView(muted(n.summary, 12));
                c.addView(smallBtn("Abrir", color, v -> lesson(n)));
                results.addView(c);
                shown++;
                if (q.isEmpty() && shown >= 8) break;
                if (!q.isEmpty() && shown >= 30) break;
            }
        }
        if (shown == 0) results.addView(muted("Nenhum tópico encontrado.", 14));
    }

    private void reviewHub() {
        LinearLayout x = root();
        backHeader(x, "Início > revisões", this::home);
        x.addView(tv("Revisões", 27, true));
        ArrayList<StudyCatalog.Node> due = new ArrayList<>();
        for (StudyCatalog.Node n : nodes.values()) {
            if (nodeMastery(n.id) > 0 && isDue(n.id)) due.add(n);
        }
        if (due.isEmpty()) {
            x.addView(muted("Nenhuma revisão está vencida agora. Continue estudando; respostas corretas criam os próximos intervalos.", 14));
        } else {
            due.sort(Comparator.comparingLong(a -> prefs.getLong("due::" + a.id, 0)));
            for (StudyCatalog.Node n : due) {
                x.addView(btn("↻ " + n.subject + " • " + n.title, AMBER, v -> lesson(n)));
            }
        }
        setContentView(page(x));
    }

    private void progressScreen() {
        LinearLayout x = root();
        backHeader(x, "Início > progresso", this::home);
        x.addView(tv("Meu progresso", 27, true));
        LinearLayout general = card();
        general.addView(tv("Geral", 18, true));
        addMetric(general, "Cobertura", coveragePercent(null));
        addMetric(general, "Domínio", masteryPercent(null));
        addMetric(general, "Precisão", accuracyPercent());
        addMetric(general, "Prontidão interna", readinessPercent(null));
        x.addView(general);

        for (String subject : StudyCatalog.SUBJECT_ORDER) {
            int color = subjectColor(subject);
            LinearLayout c = tintedCard(color);
            c.addView(tv(subject, 18, true));
            c.addView(tv("Cobertura " + coveragePercent(subject) + "% • domínio " + masteryPercent(subject) + "% • precisão " + subjectAccuracy(subject) + "%", 13, false));
            c.addView(progress(readinessPercent(subject)));
            x.addView(c);
        }

        x.addView(btn("Apagar somente o progresso da 0.13", RED, v -> new AlertDialog.Builder(this)
                .setTitle("Apagar progresso da 0.13?")
                .setMessage("Isso zera somente os dados desta versão.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Apagar", (d, w) -> { prefs.edit().clear().apply(); fontBoost = 0f; home(); })
                .show()));
        setContentView(page(x));
    }

    private int indexInSubject(StudyCatalog.Node n) {
        ArrayList<StudyCatalog.Node> list = bySubject.get(n.subject);
        if (list == null) return 0;
        for (int i = 0; i < list.size(); i++) if (list.get(i).id.equals(n.id)) return i;
        return 0;
    }

    private StudyCatalog.Node previousInSubject(StudyCatalog.Node n) {
        ArrayList<StudyCatalog.Node> list = bySubject.get(n.subject);
        int i = indexInSubject(n);
        return list != null && i > 0 ? list.get(i - 1) : null;
    }

    private StudyCatalog.Node nextInSubject(StudyCatalog.Node n) {
        ArrayList<StudyCatalog.Node> list = bySubject.get(n.subject);
        int i = indexInSubject(n);
        return list != null && i >= 0 && i + 1 < list.size() ? list.get(i + 1) : null;
    }

    private int indexInFoundation(String id) {
        int index = 0;
        for (String fid : StudyCatalog.FOUNDATION) {
            if (!nodes.containsKey(fid)) continue;
            if (fid.equals(id)) return index;
            index++;
        }
        return -1;
    }

    private int foundationCount() {
        int count = 0;
        for (String id : StudyCatalog.FOUNDATION) if (nodes.containsKey(id)) count++;
        return count;
    }

    private StudyCatalog.Node previousFoundation(StudyCatalog.Node n) {
        int idx = indexInFoundation(n.id);
        if (idx <= 0) return null;
        int seen = 0;
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node x = nodes.get(id);
            if (x == null) continue;
            if (seen == idx - 1) return x;
            seen++;
        }
        return null;
    }

    private StudyCatalog.Node nextFoundation(StudyCatalog.Node n) {
        int idx = indexInFoundation(n.id);
        if (idx < 0) return null;
        int seen = 0;
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node x = nodes.get(id);
            if (x == null) continue;
            if (seen == idx + 1) return x;
            seen++;
        }
        return null;
    }

    private void markSeen(String id) {
        prefs.edit().putBoolean("seen::" + id, true).apply();
    }

    private void recordAttempt(String id, boolean correct, String phase) {
        int allAttempts = prefs.getInt("attempts", 0) + 1;
        int allCorrect = prefs.getInt("correct", 0) + (correct ? 1 : 0);
        int nodeAttempts = prefs.getInt("attempts::" + id, 0) + 1;
        int nodeCorrect = prefs.getInt("correct::" + id, 0) + (correct ? 1 : 0);
        int mastery = nodeMastery(id);
        if (correct) {
            mastery = Math.min(5, mastery + ("transfer".equals(phase) ? 2 : 1));
        } else if ("transfer".equals(phase)) {
            mastery = Math.max(0, mastery - 1);
        }
        long now = System.currentTimeMillis();
        int days = correct ? (mastery >= 5 ? 14 : mastery >= 4 ? 7 : mastery >= 3 ? 3 : 1) : 0;
        prefs.edit()
                .putInt("attempts", allAttempts)
                .putInt("correct", allCorrect)
                .putInt("attempts::" + id, nodeAttempts)
                .putInt("correct::" + id, nodeCorrect)
                .putInt("mastery::" + id, mastery)
                .putLong("last::" + id, now)
                .putLong("due::" + id, now + days * 86_400_000L)
                .apply();
    }

    private void schedule(String id, int days) {
        prefs.edit().putLong("due::" + id, System.currentTimeMillis() + days * 86_400_000L).apply();
    }

    private int nodeMastery(String id) {
        return prefs.getInt("mastery::" + id, 0);
    }

    private int nodePercent(String id) {
        boolean seen = prefs.getBoolean("seen::" + id, false);
        int m = nodeMastery(id);
        int a = prefs.getInt("attempts::" + id, 0);
        int c = prefs.getInt("correct::" + id, 0);
        int accuracy = a == 0 ? 0 : Math.round(c * 100f / a);
        int value = (seen ? 20 : 0) + Math.round(m * 12f) + Math.round(accuracy * 0.20f);
        return Math.max(0, Math.min(100, value));
    }

    private boolean isDue(String id) {
        long due = prefs.getLong("due::" + id, 0);
        return due > 0 && System.currentTimeMillis() >= due;
    }

    private ArrayList<StudyCatalog.Node> filtered(String subject) {
        ArrayList<StudyCatalog.Node> list = new ArrayList<>();
        if (subject == null) list.addAll(nodes.values());
        else if (bySubject.containsKey(subject)) list.addAll(bySubject.get(subject));
        return list;
    }

    private int coveragePercent(String subject) {
        ArrayList<StudyCatalog.Node> list = filtered(subject);
        if (list.isEmpty()) return 0;
        int seen = 0;
        for (StudyCatalog.Node n : list) if (prefs.getBoolean("seen::" + n.id, false)) seen++;
        return Math.round(seen * 100f / list.size());
    }

    private int masteryPercent(String subject) {
        ArrayList<StudyCatalog.Node> list = filtered(subject);
        if (list.isEmpty()) return 0;
        int sum = 0;
        for (StudyCatalog.Node n : list) sum += nodeMastery(n.id) * 20;
        return Math.round(sum / (float) list.size());
    }

    private int accuracyPercent() {
        int a = prefs.getInt("attempts", 0);
        return a == 0 ? 0 : Math.round(prefs.getInt("correct", 0) * 100f / a);
    }

    private int subjectAccuracy(String subject) {
        int a = 0, c = 0;
        for (StudyCatalog.Node n : filtered(subject)) {
            a += prefs.getInt("attempts::" + n.id, 0);
            c += prefs.getInt("correct::" + n.id, 0);
        }
        return a == 0 ? 0 : Math.round(c * 100f / a);
    }

    private int readinessPercent(String subject) {
        int coverage = coveragePercent(subject);
        int mastery = masteryPercent(subject);
        int accuracy = subject == null ? accuracyPercent() : subjectAccuracy(subject);
        return Math.round(coverage * 0.30f + mastery * 0.45f + accuracy * 0.25f);
    }

    private StudyCatalog.Node recommendNext() {
        for (StudyCatalog.Node n : nodes.values()) {
            if (nodeMastery(n.id) > 0 && isDue(n.id)) return n;
        }
        // Primeiro avance para algo que ainda não foi aberto.
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node n = nodes.get(id);
            if (n != null && !prefs.getBoolean("seen::" + id, false)) return n;
        }
        // Só depois volte a tópicos abertos mas ainda fracos.
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node n = nodes.get(id);
            if (n != null && nodeMastery(id) < 3) return n;
        }
        for (StudyCatalog.Node n : nodes.values()) {
            if (!prefs.getBoolean("seen::" + n.id, false)) return n;
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
