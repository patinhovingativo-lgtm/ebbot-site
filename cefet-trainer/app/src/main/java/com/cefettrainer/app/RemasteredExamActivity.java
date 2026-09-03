package com.cefettrainer.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;

/**
 * Simulado 30 questões: 10 Português, 10 Matemática, 5 Natureza e 5 Humanas.
 * O feedback fica bloqueado até a entrega do simulado.
 */
public class RemasteredExamActivity extends Activity {
    private final int NAVY = Color.rgb(16,43,69);
    private final int BLUE = Color.rgb(31,92,155);
    private final int GREEN = Color.rgb(33,128,91);
    private final int RED = Color.rgb(181,55,55);
    private final int AMBER = Color.rgb(176,113,15);
    private final int BG = Color.rgb(245,247,250);
    private final int TEXT = Color.rgb(28,38,48);
    private final int MUTED = Color.rgb(82,94,105);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ArrayList<ExamItem> exam;
    private int[] answers;
    private int index = 0;
    private long endTime;
    private TextView timer;
    private SharedPreferences prefs;
    private final Random random = new Random();

    private static final class ExamItem {
        final StudyCatalog.Node node;
        final StudyCatalog.Question q;
        final String area;
        ExamItem(StudyCatalog.Node node, String area) {
            this.node = node;
            this.q = node.transfer;
            this.area = area;
        }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        prefs = getSharedPreferences("cefet_v12_remaster", MODE_PRIVATE);
        exam = buildExam();
        answers = new int[exam.size()];
        Arrays.fill(answers, -1);
        endTime = System.currentTimeMillis() + 4L * 60L * 60L * 1000L;
        showQuestion();
        handler.post(ticker);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(ticker);
        super.onDestroy();
    }

    private ArrayList<ExamItem> buildExam() {
        LinkedHashMap<String, StudyCatalog.Node> all = StudyCatalog.create();
        ArrayList<StudyCatalog.Node> p = new ArrayList<>();
        ArrayList<StudyCatalog.Node> m = new ArrayList<>();
        ArrayList<StudyCatalog.Node> n = new ArrayList<>();
        ArrayList<StudyCatalog.Node> h = new ArrayList<>();
        for (StudyCatalog.Node node : all.values()) {
            if (node.transfer == null) continue;
            if ("Português".equals(node.subject)) p.add(node);
            else if ("Matemática".equals(node.subject)) m.add(node);
            else if ("Física".equals(node.subject) || "Química".equals(node.subject) || "Biologia".equals(node.subject)) n.add(node);
            else if ("História".equals(node.subject) || "Geografia".equals(node.subject)) h.add(node);
        }
        Collections.shuffle(p, random);
        Collections.shuffle(m, random);
        Collections.shuffle(n, random);
        Collections.shuffle(h, random);
        ArrayList<ExamItem> out = new ArrayList<>();
        take(out, p, 10, "Língua Portuguesa");
        take(out, m, 10, "Matemática");
        take(out, n, 5, "Ciências da Natureza");
        take(out, h, 5, "Ciências Humanas");
        // Mantém a distribuição por área, mas embaralha apenas dentro dos blocos para ficar previsível ao candidato.
        return out;
    }

    private void take(ArrayList<ExamItem> out, ArrayList<StudyCatalog.Node> source, int count, String area) {
        for (int i = 0; i < Math.min(count, source.size()); i++) out.add(new ExamItem(source.get(i), area));
    }

    private void showQuestion() {
        if (exam.isEmpty()) {
            LinearLayout x = root();
            x.addView(text("Banco de questões indisponível nesta compilação.", 20, true));
            x.addView(button("Voltar", NAVY, v -> finish()));
            setContentView(page(x));
            return;
        }
        ExamItem item = exam.get(index);
        LinearLayout x = root();
        LinearLayout header = card();
        header.addView(text("Simulado CEFET — 30 questões", 20, true));
        timer = text("04:00:00", 22, true);
        timer.setTextColor(NAVY);
        header.addView(timer);
        header.addView(text("Questão " + (index+1) + " de " + exam.size() + " • " + item.area, 15, true));
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(exam.size());
        pb.setProgress(index+1);
        header.addView(pb, new LinearLayout.LayoutParams(-1, dp(18)));
        x.addView(header);

        LinearLayout qcard = card();
        qcard.addView(text(item.node.subject + " • " + item.node.title, 14, true));
        qcard.addView(text(item.q.prompt, 18, true));
        RadioGroup g = new RadioGroup(this);
        g.setOrientation(RadioGroup.VERTICAL);
        for (int i = 0; i < 4; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setId(9000+i);
            rb.setText((char)('A'+i) + ") " + item.q.options[i]);
            rb.setTextSize(16);
            rb.setTextColor(TEXT);
            rb.setPadding(dp(7),dp(8),dp(7),dp(8));
            g.addView(rb);
            if (answers[index] == i) rb.setChecked(true);
        }
        g.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId >= 9000 && checkedId <= 9003) answers[index] = checkedId - 9000;
        });
        qcard.addView(g);
        x.addView(qcard);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        Button prev = smallButton("← Anterior", Color.rgb(92,103,114), v -> { if (index>0) {index--;showQuestion();} });
        prev.setEnabled(index>0);
        Button next = smallButton(index == exam.size()-1 ? "Entregar" : "Próxima →", index == exam.size()-1 ? GREEN : BLUE, v -> {
            if (index == exam.size()-1) confirmFinish();
            else { index++; showQuestion(); }
        });
        nav.addView(prev, new LinearLayout.LayoutParams(0,-2,1));
        nav.addView(next, new LinearLayout.LayoutParams(0,-2,1));
        x.addView(nav);
        x.addView(button("Entregar simulado agora", AMBER, v -> confirmFinish()));
        x.addView(button("Sair sem corrigir", RED, v -> finish()));
        setContentView(page(x));
        updateTimer();
    }

    private void confirmFinish() {
        int blank = 0;
        for (int a : answers) if (a < 0) blank++;
        String msg = blank == 0 ? "Todas as questões têm resposta." : "Ainda há " + blank + " questão(ões) em branco.";
        new AlertDialog.Builder(this)
                .setTitle("Entregar simulado?")
                .setMessage(msg + " Depois da entrega, o gabarito e as explicações serão mostrados.")
                .setNegativeButton("Continuar", null)
                .setPositiveButton("Entregar", (d,w) -> finishExam())
                .show();
    }

    private void finishExam() {
        handler.removeCallbacks(ticker);
        int correct = 0;
        LinkedHashMap<String,int[]> area = new LinkedHashMap<>();
        for (int i = 0; i < exam.size(); i++) {
            ExamItem it = exam.get(i);
            if (!area.containsKey(it.area)) area.put(it.area, new int[]{0,0});
            area.get(it.area)[1]++;
            if (answers[i] == it.q.correct) {
                correct++;
                area.get(it.area)[0]++;
            }
        }
        int score = correct * 5;
        int count = prefs.getInt("exam_count",0)+1;
        int best = Math.max(prefs.getInt("exam_best",0), score);
        prefs.edit().putInt("exam_count",count).putInt("exam_best",best).putInt("exam_last",score).apply();

        LinearLayout x = root();
        x.addView(text("Resultado do simulado", 27, true));
        LinearLayout summary = card();
        summary.addView(text(correct + " / " + exam.size() + " acertos", 23, true));
        summary.addView(text(score + " / 150 pontos objetivos", 20, true));
        summary.addView(muted("Isto é pontuação do treino. Não substitui classificação oficial e não inclui a nota da redação.", 13));
        for (Map.Entry<String,int[]> e : area.entrySet()) {
            summary.addView(text(e.getKey() + ": " + e.getValue()[0] + "/" + e.getValue()[1], 15, false));
        }
        summary.addView(text("Melhor resultado salvo: " + best + "/150", 14, true));
        x.addView(summary);

        for (int i = 0; i < exam.size(); i++) {
            ExamItem it = exam.get(i);
            int chosen = answers[i];
            boolean ok = chosen == it.q.correct;
            LinearLayout c = card();
            TextView title = text((i+1) + ". " + it.node.title + (ok ? " • ✓" : " • ✗"), 16, true);
            title.setTextColor(ok ? GREEN : RED);
            c.addView(title);
            c.addView(text(it.q.prompt, 14, false));
            if (chosen < 0) c.addView(muted("Sua resposta: em branco", 13));
            else c.addView(text("Sua resposta: " + (char)('A'+chosen) + ") " + it.q.options[chosen] + " — " + it.q.feedback[chosen], 13, false));
            if (!ok) {
                TextView good = text("Correta: " + (char)('A'+it.q.correct) + ") " + it.q.options[it.q.correct] + " — " + it.q.feedback[it.q.correct], 13, false);
                good.setTextColor(GREEN);
                c.addView(good);
            }
            c.addView(smallButton("Estudar este tópico", BLUE, v -> {
                Intent in = new Intent(this, RemasteredActivity.class);
                in.putExtra("open_node", it.node.id);
                startActivity(in);
            }));
            x.addView(c);
        }
        x.addView(button("Novo simulado", GREEN, v -> recreate()));
        x.addView(button("Voltar ao tutor", NAVY, v -> finish()));
        setContentView(page(x));
    }

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            long left = endTime - System.currentTimeMillis();
            if (left <= 0) {
                if (timer != null) timer.setText("00:00:00");
                finishExam();
                return;
            }
            updateTimer();
            handler.postDelayed(this, 1000);
        }
    };

    private void updateTimer() {
        if (timer == null) return;
        long s = Math.max(0, (endTime-System.currentTimeMillis())/1000);
        timer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", s/3600, (s%3600)/60, s%60));
    }

    private LinearLayout root() {
        LinearLayout x = new LinearLayout(this);
        x.setOrientation(LinearLayout.VERTICAL);
        x.setPadding(dp(16),dp(16),dp(16),dp(90));
        x.setBackgroundColor(BG);
        return x;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14),dp(12),dp(14),dp(12));
        c.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(7),0,dp(7));
        c.setLayoutParams(p);
        return c;
    }

    private TextView text(String s, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(TEXT);
        v.setPadding(dp(10),dp(8),dp(10),dp(8));
        v.setLineSpacing(dp(2),1.08f);
        if (bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return v;
    }

    private TextView muted(String s, int size) {
        TextView v = text(s,size,false);
        v.setTextColor(MUTED);
        return v;
    }

    private Button button(String label, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(5),0,dp(5));
        b.setLayoutParams(p);
        return b;
    }

    private Button smallButton(String label, int color, View.OnClickListener l) {
        Button b = button(label,color,l);
        b.setTextSize(13);
        return b;
    }

    private ScrollView page(LinearLayout x) {
        Space s0 = new Space(this);
        x.addView(s0,new LinearLayout.LayoutParams(1,dp(100)));
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setVerticalScrollBarEnabled(true);
        s.setScrollbarFadingEnabled(false);
        s.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        s.addView(x);
        return s;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
