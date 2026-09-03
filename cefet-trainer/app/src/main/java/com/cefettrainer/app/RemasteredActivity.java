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

import java.util.*;

/**
 * CEFET Trainer Remaster.
 *
 * A tela foi reconstruída para ensinar conceitos pequenos em cadeia, com pergunta
 * no meio da explicação, feedback instantâneo por alternativa, rota de pré-requisito,
 * resumos memorizáveis e métricas separadas de cobertura/domínio/retenção/precisão.
 */
public class RemasteredActivity extends Activity {
    private final int NAVY = Color.rgb(16, 43, 69);
    private final int BLUE = Color.rgb(31, 92, 155);
    private final int GREEN = Color.rgb(33, 128, 91);
    private final int RED = Color.rgb(181, 55, 55);
    private final int AMBER = Color.rgb(176, 113, 15);
    private final int PURPLE = Color.rgb(108, 72, 145);
    private final int BG = Color.rgb(245, 247, 250);
    private final int TEXT = Color.rgb(28, 38, 48);
    private final int MUTED = Color.rgb(82, 94, 105);
    private final int SOFT = Color.rgb(234, 239, 245);

    private LinkedHashMap<String, StudyCatalog.Node> nodes;
    private LinkedHashMap<String, ArrayList<StudyCatalog.Node>> bySubject;
    private SharedPreferences prefs;
    private float fontBoost = 0f;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        prefs = getSharedPreferences("cefet_v12_remaster", MODE_PRIVATE);
        fontBoost = prefs.getBoolean("large_font", false) ? 2f : 0f;
        nodes = StudyCatalog.create();
        indexSubjects();
        home();
    }

    private void indexSubjects() {
        bySubject = new LinkedHashMap<>();
        for (String s : StudyCatalog.SUBJECT_ORDER) bySubject.put(s, new ArrayList<>());
        for (StudyCatalog.Node n : nodes.values()) {
            if (!bySubject.containsKey(n.subject)) bySubject.put(n.subject, new ArrayList<>());
            bySubject.get(n.subject).add(n);
        }
    }

    private int sp(int base) { return Math.round(base + fontBoost); }

    private TextView text(String s, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp(size));
        v.setTextColor(TEXT);
        v.setPadding(dp(12), dp(9), dp(12), dp(9));
        v.setLineSpacing(dp(2), 1.08f);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private TextView muted(String s, int size) {
        TextView v = text(s, size, false);
        v.setTextColor(MUTED);
        return v;
    }

    private Button button(String label, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(sp(15));
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setMinHeight(dp(52));
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setPadding(dp(14), dp(8), dp(14), dp(8));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(5), 0, dp(5));
        b.setLayoutParams(p);
        return b;
    }

    private Button compactButton(String label, int color, View.OnClickListener l) {
        Button b = button(label, color, l);
        b.setTextSize(sp(13));
        b.setMinHeight(dp(44));
        return b;
    }

    private LinearLayout root() {
        LinearLayout x = new LinearLayout(this);
        x.setOrientation(LinearLayout.VERTICAL);
        x.setBackgroundColor(BG);
        x.setPadding(dp(16), dp(16), dp(16), dp(96));
        return x;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(7), 0, dp(7));
        c.setLayoutParams(p);
        return c;
    }

    private ScrollView page(LinearLayout x) {
        Space spacer = new Space(this);
        x.addView(spacer, new LinearLayout.LayoutParams(1, dp(120)));
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setVerticalScrollBarEnabled(true);
        s.setScrollbarFadingEnabled(false);
        s.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        s.setSmoothScrollingEnabled(true);
        s.addView(x, new ScrollView.LayoutParams(-1, -2));
        return s;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private void breadcrumb(LinearLayout x, String label, Runnable back) {
        x.addView(compactButton("← Voltar", NAVY, v -> back.run()));
        TextView b = muted(label, 13);
        b.setTextColor(BLUE);
        x.addView(b);
    }

    private ProgressBar bar(int percent) {
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(Math.max(0, Math.min(100, percent)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(20));
        p.setMargins(dp(8), dp(3), dp(8), dp(6));
        pb.setLayoutParams(p);
        return pb;
    }

    private void home() {
        LinearLayout x = root();
        x.addView(text("CEFET Trainer — Remaster", 29, true));
        x.addView(muted("Aprender a base → detectar lacunas → reparar → transferir → revisar → simular", 14));

        LinearLayout official = card();
        official.addView(text("Formato de treino CEFET/RJ 2027", 18, true));
        official.addView(text("30 questões objetivas + redação • 4 horas", 15, false));
        official.addView(muted("O tutor não tenta adivinhar as perguntas da prova. Ele ensina habilidades e pré-requisitos para você resolver questões novas.", 13));
        x.addView(official);

        int coverage = coveragePercent(null);
        int mastery = masteryPercent(null);
        int retention = retentionPercent(null);
        int accuracy = accuracyPercent();
        int ready = readinessPercent(null);
        LinearLayout stats = card();
        stats.addView(text("Seu painel não usa uma porcentagem falsa única", 18, true));
        addMetric(stats, "Cobertura", coverage, "quanto do tutor você já abriu");
        addMetric(stats, "Domínio", mastery, "quanto você demonstrou entender");
        addMetric(stats, "Retenção", retention, "quanto parece ainda recuperável após o tempo");
        addMetric(stats, "Precisão", accuracy, "acertos nas checagens e transferências");
        addMetric(stats, "Prontidão", ready, "indicador interno combinando as métricas; não é nota oficial");
        x.addView(stats);

        StudyCatalog.Node next = recommendNext();
        if (next != null) {
            LinearLayout nextCard = card();
            nextCard.addView(text("Próximo passo sugerido", 18, true));
            nextCard.addView(text(next.subject + " • " + next.title, 16, true));
            nextCard.addView(muted(next.summary, 14));
            nextCard.addView(compactButton("Abrir este tópico", GREEN, v -> lesson(next)));
            x.addView(nextCard);
        }

        x.addView(button("Reconstruir minha base", GREEN, v -> foundation()));
        x.addView(button("Aprender por matéria", BLUE, v -> subjectList()));
        x.addView(button("Buscar tópico ou palavra", Color.rgb(72, 94, 160), v -> searchScreen()));
        x.addView(button("Revisões vencidas", AMBER, v -> reviewHub()));
        x.addView(button("Simulado remaster — 30 questões", NAVY, v -> startActivity(new Intent(this, RemasteredExamActivity.class))));
        x.addView(button("Redação — foto/OCR + treino", PURPLE, v -> startActivity(new Intent(this, EssayCoachActivity.class))));
        x.addView(button("Mapa completo antigo do programa", Color.rgb(92, 103, 114), v -> startActivity(new Intent(this, V10Activity.class))));
        x.addView(button("Progresso detalhado", Color.rgb(80, 92, 102), v -> progressScreen()));
        x.addView(compactButton(prefs.getBoolean("large_font", false) ? "Fonte maior: ligada" : "Fonte maior: desligada", Color.rgb(105, 112, 120), v -> {
            boolean on = !prefs.getBoolean("large_font", false);
            prefs.edit().putBoolean("large_font", on).apply();
            fontBoost = on ? 2f : 0f;
            home();
        }));
        setContentView(page(x));
    }

    private void addMetric(LinearLayout box, String name, int pct, String desc) {
        box.addView(text(name + " • " + pct + "%", 15, true));
        box.addView(bar(pct));
        box.addView(muted(desc, 12));
    }

    private void subjectList() {
        LinearLayout x = root();
        breadcrumb(x, "Início > matérias", this::home);
        x.addView(text("Aprender por matéria", 26, true));
        x.addView(muted("Cada matéria mostra uma sequência. Você pode entrar num tópico avançado, mas o app lista os pré-requisitos que faltam.", 14));
        for (String s : StudyCatalog.SUBJECT_ORDER) {
            int pct = readinessPercent(s);
            ArrayList<StudyCatalog.Node> list = bySubject.get(s);
            String count = list == null ? "0" : String.valueOf(list.size());
            x.addView(button(s + " • " + pct + "% prontidão • " + count + " microlições", BLUE, v -> subjectScreen(s)));
        }
        setContentView(page(x));
    }

    private void subjectScreen(String subject) {
        LinearLayout x = root();
        breadcrumb(x, "Início > " + subject, this::subjectList);
        x.addView(text(subject, 27, true));
        LinearLayout intro = card();
        intro.addView(text("Resumo da matéria", 18, true));
        intro.addView(text(StudyCatalog.subjectIntro(subject), 15, false));
        intro.addView(text("Prontidão atual: " + readinessPercent(subject) + "%", 15, true));
        intro.addView(bar(readinessPercent(subject)));
        x.addView(intro);

        ArrayList<StudyCatalog.Node> list = bySubject.get(subject);
        if (list == null || list.isEmpty()) x.addView(muted("Nenhuma microlição nesta versão.", 14));
        else {
            for (StudyCatalog.Node n : list) {
                int m = nodeMastery(n.id);
                String status = m == 0 ? "novo" : (m >= 4 ? "forte" : (isDue(n.id) ? "revisar" : "em treino"));
                int color = isDue(n.id) && m > 0 ? AMBER : (m >= 4 ? GREEN : BLUE);
                x.addView(button(n.title + " • " + nodePercent(n.id) + "% • " + status, color, v -> lesson(n)));
            }
        }
        setContentView(page(x));
    }

    private void foundation() {
        LinearLayout x = root();
        breadcrumb(x, "Início > reconstrução da base", this::home);
        x.addView(text("Reconstruir a base", 27, true));
        x.addView(muted("Esta trilha não presume que você lembra termos antigos. Ela começa em palavra/substantivo/operações e sobe até equações, funções e leitura de prova.", 14));
        int i = 1;
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node n = nodes.get(id);
            if (n == null) continue;
            String prefix = String.format(Locale.getDefault(), "%02d. ", i++);
            int color = nodeMastery(id) >= 4 ? GREEN : (isDue(id) ? AMBER : BLUE);
            x.addView(button(prefix + n.subject + " • " + n.title + " • " + nodePercent(id) + "%", color, v -> lesson(n)));
        }
        setContentView(page(x));
    }

    private void lesson(StudyCatalog.Node n) {
        markSeen(n.id);
        LinearLayout x = root();
        breadcrumb(x, "Início > " + n.subject + " > " + n.title, () -> subjectScreen(n.subject));
        x.addView(text(n.title, 25, true));
        x.addView(muted("Nível " + n.level + " • domínio do tópico: " + nodePercent(n.id) + "%", 13));

        if (n.prerequisites.length > 0) {
            LinearLayout pre = card();
            pre.addView(text("Antes disso", 17, true));
            boolean missing = false;
            for (String pid : n.prerequisites) {
                StudyCatalog.Node p = nodes.get(pid);
                if (p == null) continue;
                int mp = nodeMastery(pid);
                if (mp < 2) missing = true;
                pre.addView(compactButton((mp < 2 ? "⚠ " : "✓ ") + p.title + " • " + nodePercent(pid) + "%", mp < 2 ? AMBER : GREEN, v -> lesson(p)));
            }
            pre.addView(muted(missing ? "Há base ainda fraca. Você pode continuar, mas o app vai apontar a lacuna se ela aparecer na resposta." : "Pré-requisitos essenciais já foram praticados.", 12));
            x.addView(pre);
        }

        LinearLayout memory = card();
        memory.addView(text("Resumo de 20 segundos", 18, true));
        memory.addView(text(n.memory, 17, true));
        x.addView(memory);

        LinearLayout simple = card();
        simple.addView(text("Comece simples", 18, true));
        simple.addView(text(n.summary, 16, false));
        x.addView(simple);

        LinearLayout terms = card();
        terms.addView(text("Palavras que precisam estar claras", 17, true));
        terms.addView(text(n.terms, 14, false));
        terms.addView(compactButton("Não sei uma palavra", Color.rgb(86, 99, 167), v -> wordHelp(n)));
        x.addView(terms);

        LinearLayout worked = card();
        worked.addView(text("Exemplo resolvido — acompanhe a lógica", 18, true));
        worked.addView(text(n.worked, 15, false));
        x.addView(worked);

        LinearLayout checkpoint = card();
        checkpoint.addView(text("AGORA TESTE NO MEIO DA EXPLICAÇÃO", 18, true));
        checkpoint.addView(muted("Toque numa alternativa. Não existe botão 'verificar': a explicação aparece na hora.", 13));
        renderQuestion(checkpoint, n, n.check, "check");
        x.addView(checkpoint);

        LinearLayout deep = card();
        deep.addView(text("Aprofundar", 18, true));
        TextView detail = text(n.detail, 15, false);
        detail.setVisibility(View.GONE);
        deep.addView(compactButton("Mostrar explicação completa", BLUE, v -> {
            detail.setVisibility(detail.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            ((Button)v).setText(detail.getVisibility() == View.VISIBLE ? "Esconder explicação completa" : "Mostrar explicação completa");
        }));
        deep.addView(detail);
        x.addView(deep);

        LinearLayout trap = card();
        trap.addView(text("Contraexemplo", 17, true));
        trap.addView(text(n.counterExample, 14, false));
        trap.addView(text("Armadilha comum", 17, true));
        trap.addView(text(n.trap, 14, false));
        x.addView(trap);

        LinearLayout transfer = card();
        transfer.addView(text("Questão de transferência", 18, true));
        transfer.addView(muted("Agora a ideia aparece em outro contexto. Esta parte deve se aproximar mais do raciocínio de prova.", 13));
        renderQuestion(transfer, n, n.transfer, "transfer");
        x.addView(transfer);

        x.addView(button("Marcar para revisar amanhã", AMBER, v -> {
            schedule(n.id, 1);
            Toast.makeText(this, "Revisão marcada.", Toast.LENGTH_SHORT).show();
        }));
        x.addView(button("Próximo tópico recomendado", GREEN, v -> {
            StudyCatalog.Node nx = recommendNext();
            if (nx != null) lesson(nx); else home();
        }));
        setContentView(page(x));
    }

    private void renderQuestion(LinearLayout box, StudyCatalog.Node n, StudyCatalog.Question q, String phase) {
        if (q == null) return;
        box.addView(text(q.prompt, 16, true));
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        ArrayList<RadioButton> radios = new ArrayList<>();
        HashMap<Integer, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < q.options.length; i++) {
            RadioButton rb = new RadioButton(this);
            int id = View.generateViewId();
            rb.setId(id);
            idToIndex.put(id, i);
            rb.setText((char)('A' + i) + ") " + q.options[i]);
            rb.setTextSize(sp(15));
            rb.setTextColor(TEXT);
            rb.setPadding(dp(6), dp(7), dp(6), dp(7));
            group.addView(rb);
            radios.add(rb);
        }
        box.addView(group);
        LinearLayout feedback = card();
        feedback.setBackgroundColor(SOFT);
        feedback.setVisibility(View.GONE);
        box.addView(feedback);

        final boolean[] answered = {false};
        group.setOnCheckedChangeListener((g, checkedId) -> {
            if (answered[0]) return;
            Integer chosenObj = idToIndex.get(checkedId);
            if (chosenObj == null) return;
            answered[0] = true;
            int chosen = chosenObj;
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
            boolean correct = chosen == q.correct;
            recordAttempt(n.id, correct, phase);
            TextView result = text(correct ? "✓ Acertou" : "✗ Essa escolha não funciona", 18, true);
            result.setTextColor(correct ? GREEN : RED);
            feedback.addView(result);
            TextView chosenWhy = text("Sua alternativa: " + q.feedback[chosen], 14, false);
            chosenWhy.setTextColor(correct ? GREEN : RED);
            feedback.addView(chosenWhy);
            if (!correct) {
                TextView good = text("Correta: " + q.options[q.correct] + " — " + q.feedback[q.correct], 14, false);
                good.setTextColor(GREEN);
                feedback.addView(good);
            }
            feedback.addView(text("Por que cada alternativa está certa ou errada", 15, true));
            for (int i = 0; i < q.options.length; i++) {
                String prefix = (i == q.correct ? "✓ " : "• ") + (char)('A' + i) + ": ";
                TextView why = muted(prefix + q.feedback[i], 12);
                if (i == q.correct) why.setTextColor(GREEN);
                feedback.addView(why);
            }
            String route = chosen < q.route.length ? q.route[chosen] : null;
            if (!correct && route != null && nodes.containsKey(route)) {
                StudyCatalog.Node p = nodes.get(route);
                feedback.addView(text("Lacuna provável detectada", 15, true));
                feedback.addView(muted("Sua escolha combina com uma dificuldade anterior em: " + p.title + ".", 13));
                feedback.addView(compactButton("Corrigir esta lacuna agora", AMBER, v -> lesson(p)));
            }
        });
    }

    private void wordHelp(StudyCatalog.Node current) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));
        EditText input = new EditText(this);
        input.setHint("Digite: substantivo, singular, domínio, razão...");
        input.setSingleLine(true);
        box.addView(input);
        TextView result = text("Digite uma palavra e toque em procurar.", 14, false);
        box.addView(result);
        new AlertDialog.Builder(this)
                .setTitle("Não sei esta palavra")
                .setView(box)
                .setPositiveButton("Procurar", null)
                .setNegativeButton("Fechar", null)
                .create()
                .setOnShowListener(d -> {
                    AlertDialog dialog = (AlertDialog)d;
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        String q = normalize(input.getText().toString());
                        LinkedHashMap<String,String> glossary = StudyCatalog.glossary();
                        String found = null;
                        String key = null;
                        for (Map.Entry<String,String> e : glossary.entrySet()) {
                            if (normalize(e.getKey()).contains(q) || q.contains(normalize(e.getKey()))) {
                                key = e.getKey(); found = e.getValue(); break;
                            }
                        }
                        if (q.trim().isEmpty()) result.setText("Digite uma palavra.");
                        else if (found != null) result.setText(key.toUpperCase(Locale.ROOT) + "\n\n" + found);
                        else result.setText("Ainda não há verbete específico. Leia a seção 'Palavras que precisam estar claras' deste tópico ou use a busca do app.");
                    });
                });
        // O create/setOnShow acima não chama show; criamos de novo corretamente abaixo.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Não sei esta palavra")
                .setView(box)
                .setPositiveButton("Procurar", null)
                .setNegativeButton("Fechar", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String q = normalize(input.getText().toString());
            LinkedHashMap<String,String> glossary = StudyCatalog.glossary();
            String found = null, key = null;
            for (Map.Entry<String,String> e : glossary.entrySet()) {
                if (!q.isEmpty() && (normalize(e.getKey()).contains(q) || q.contains(normalize(e.getKey())))) {
                    key = e.getKey(); found = e.getValue(); break;
                }
            }
            if (q.isEmpty()) result.setText("Digite uma palavra.");
            else if (found != null) result.setText(key.toUpperCase(Locale.ROOT) + "\n\n" + found);
            else result.setText("Não achei um verbete específico. Tente outra forma da palavra ou abra a busca geral.");
        }));
        dialog.show();
    }

    private void searchScreen() {
        LinearLayout x = root();
        breadcrumb(x, "Início > busca", this::home);
        x.addView(text("Buscar tópico ou palavra", 25, true));
        EditText search = new EditText(this);
        search.setHint("Ex.: substantivo, x−5=12, fração, inferência, átomo...");
        search.setSingleLine(true);
        x.addView(search, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        x.addView(results);
        Runnable refresh = () -> renderSearch(results, search.getText().toString());
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { refresh.run(); }
            public void afterTextChanged(Editable e) {}
        });
        renderSearch(results, "");
        setContentView(page(x));
    }

    private void renderSearch(LinearLayout results, String raw) {
        results.removeAllViews();
        String q = normalize(raw);
        int shown = 0;
        for (StudyCatalog.Node n : nodes.values()) {
            String hay = normalize(n.subject + " " + n.title + " " + n.summary + " " + n.terms + " " + n.memory);
            if (q.isEmpty() || hay.contains(q)) {
                results.addView(compactButton(n.subject + " • " + n.title + " • " + nodePercent(n.id) + "%", BLUE, v -> lesson(n)));
                if (++shown >= 30 && !q.isEmpty()) break;
                if (shown >= 12 && q.isEmpty()) break;
            }
        }
        if (shown == 0) results.addView(muted("Nenhum tópico encontrado. Tente uma palavra mais curta.", 14));
    }

    private void reviewHub() {
        LinearLayout x = root();
        breadcrumb(x, "Início > revisões", this::home);
        x.addView(text("Revisões vencidas", 25, true));
        ArrayList<StudyCatalog.Node> due = new ArrayList<>();
        for (StudyCatalog.Node n : nodes.values()) if (nodeMastery(n.id) > 0 && isDue(n.id)) due.add(n);
        due.sort(Comparator.comparingLong(a -> prefs.getLong("due::" + a.id, 0)));
        if (due.isEmpty()) x.addView(muted("Nenhuma revisão vencida agora. O app usa intervalos progressivos depois de respostas corretas.", 14));
        for (StudyCatalog.Node n : due) x.addView(button("↻ " + n.subject + " • " + n.title + " • " + nodePercent(n.id) + "%", AMBER, v -> lesson(n)));
        setContentView(page(x));
    }

    private void progressScreen() {
        LinearLayout x = root();
        breadcrumb(x, "Início > progresso", this::home);
        x.addView(text("Progresso detalhado", 25, true));
        LinearLayout all = card();
        addMetric(all, "Cobertura geral", coveragePercent(null), "tópicos já abertos");
        addMetric(all, "Domínio geral", masteryPercent(null), "média de domínio 0–5");
        addMetric(all, "Retenção geral", retentionPercent(null), "estimativa pelo intervalo de revisão");
        addMetric(all, "Precisão geral", accuracyPercent(), "acertos acumulados");
        addMetric(all, "Prontidão geral", readinessPercent(null), "combinação interna; não equivale à nota da prova");
        x.addView(all);
        for (String s : StudyCatalog.SUBJECT_ORDER) {
            LinearLayout c = card();
            c.addView(text(s, 18, true));
            c.addView(text("Cobertura " + coveragePercent(s) + "% • Domínio " + masteryPercent(s) + "% • Retenção " + retentionPercent(s) + "% • Prontidão " + readinessPercent(s) + "%", 14, false));
            c.addView(bar(readinessPercent(s)));
            x.addView(c);
        }
        x.addView(button("Apagar progresso do Remaster", RED, v -> new AlertDialog.Builder(this)
                .setTitle("Apagar progresso?")
                .setMessage("Apaga apenas os dados de estudo da versão Remaster. Não altera arquivos nem o app antigo.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Apagar", (d,w) -> { prefs.edit().clear().apply(); fontBoost = 0f; home(); })
                .show()));
        setContentView(page(x));
    }

    private void markSeen(String id) {
        if (!prefs.getBoolean("seen::" + id, false)) {
            prefs.edit().putBoolean("seen::" + id, true).apply();
        }
    }

    private void recordAttempt(String id, boolean correct, String phase) {
        int attempts = prefs.getInt("attempts", 0) + 1;
        int correctAll = prefs.getInt("correct", 0) + (correct ? 1 : 0);
        int nodeAttempts = prefs.getInt("attempts::" + id, 0) + 1;
        int nodeCorrect = prefs.getInt("correct::" + id, 0) + (correct ? 1 : 0);
        int m = nodeMastery(id);
        if (correct) {
            if ("transfer".equals(phase)) m = Math.min(5, Math.max(2, m + 1));
            else m = Math.min(5, Math.max(1, m + 1));
        } else {
            m = Math.max(0, m - ("transfer".equals(phase) ? 1 : 0));
        }
        long now = System.currentTimeMillis();
        int days = correct ? (m >= 5 ? 14 : m >= 4 ? 7 : m >= 3 ? 3 : 1) : 0;
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("attempts", attempts);
        e.putInt("correct", correctAll);
        e.putInt("attempts::" + id, nodeAttempts);
        e.putInt("correct::" + id, nodeCorrect);
        e.putInt("mastery::" + id, m);
        e.putLong("last::" + id, now);
        e.putLong("due::" + id, now + days * 86_400_000L);
        e.apply();
    }

    private void schedule(String id, int days) {
        prefs.edit().putLong("due::" + id, System.currentTimeMillis() + days * 86_400_000L).apply();
    }

    private int nodeMastery(String id) { return prefs.getInt("mastery::" + id, 0); }

    private int nodePercent(String id) {
        int m = nodeMastery(id);
        int a = prefs.getInt("attempts::" + id, 0);
        int c = prefs.getInt("correct::" + id, 0);
        int acc = a == 0 ? 0 : Math.round(c * 100f / a);
        int retention = nodeRetention(id);
        boolean seen = prefs.getBoolean("seen::" + id, false);
        return Math.round((seen ? 15 : 0) + (m / 5f) * 50f + acc * 0.20f + retention * 0.15f);
    }

    private int nodeRetention(String id) {
        int m = nodeMastery(id);
        if (m == 0) return 0;
        long due = prefs.getLong("due::" + id, 0);
        long last = prefs.getLong("last::" + id, 0);
        if (last == 0) return 0;
        long now = System.currentTimeMillis();
        if (due == 0 || now <= due) return Math.min(100, 45 + m * 11);
        long overdueDays = Math.max(0, (now - due) / 86_400_000L);
        return Math.max(10, Math.min(100, 45 + m * 11 - (int)overdueDays * 8));
    }

    private boolean isDue(String id) {
        long due = prefs.getLong("due::" + id, 0);
        return due > 0 && System.currentTimeMillis() >= due;
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
        return Math.round(sum / (float)list.size());
    }

    private int retentionPercent(String subject) {
        ArrayList<StudyCatalog.Node> list = filtered(subject);
        int count = 0, sum = 0;
        for (StudyCatalog.Node n : list) {
            if (nodeMastery(n.id) > 0) { sum += nodeRetention(n.id); count++; }
        }
        return count == 0 ? 0 : Math.round(sum / (float)count);
    }

    private int accuracyPercent() {
        int a = prefs.getInt("attempts", 0);
        return a == 0 ? 0 : Math.round(prefs.getInt("correct", 0) * 100f / a);
    }

    private int subjectAccuracy(String subject) {
        ArrayList<StudyCatalog.Node> list = filtered(subject);
        int a = 0, c = 0;
        for (StudyCatalog.Node n : list) {
            a += prefs.getInt("attempts::" + n.id, 0);
            c += prefs.getInt("correct::" + n.id, 0);
        }
        return a == 0 ? 0 : Math.round(c * 100f / a);
    }

    private int readinessPercent(String subject) {
        int cov = coveragePercent(subject);
        int mas = masteryPercent(subject);
        int ret = retentionPercent(subject);
        int acc = subject == null ? accuracyPercent() : subjectAccuracy(subject);
        return Math.round(cov * 0.20f + mas * 0.35f + ret * 0.25f + acc * 0.20f);
    }

    private ArrayList<StudyCatalog.Node> filtered(String subject) {
        ArrayList<StudyCatalog.Node> list = new ArrayList<>();
        if (subject == null) list.addAll(nodes.values());
        else if (bySubject.containsKey(subject)) list.addAll(bySubject.get(subject));
        return list;
    }

    private StudyCatalog.Node recommendNext() {
        // 1) revisão vencida com domínio já iniciado
        for (StudyCatalog.Node n : nodes.values()) if (nodeMastery(n.id) > 0 && isDue(n.id)) return n;
        // 2) primeiro nó da trilha de base ainda fraco
        for (String id : StudyCatalog.FOUNDATION) {
            StudyCatalog.Node n = nodes.get(id);
            if (n != null && nodeMastery(id) < 3) return n;
        }
        // 3) menor domínio geral cujos pré-requisitos não estejam muito fracos
        StudyCatalog.Node best = null;
        int bestM = 99;
        for (StudyCatalog.Node n : nodes.values()) {
            int m = nodeMastery(n.id);
            boolean prereqOk = true;
            for (String p : n.prerequisites) if (nodes.containsKey(p) && nodeMastery(p) < 2) { prereqOk = false; break; }
            if (prereqOk && m < bestM) { best = n; bestM = m; }
        }
        return best;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return x;
    }
}
