package com.cefettrainer.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Redação v0.10: OCR em múltiplas leituras, validação contra texto corrompido e avaliação de treino. */
public class EssayCoachActivity extends Activity {
    private static final int PICK=201, CAM=202;
    private final int NAVY=Color.rgb(13,43,74), BLUE=Color.rgb(28,91,150), GREEN=Color.rgb(34,131,94), RED=Color.rgb(183,61,61), PURPLE=Color.rgb(123,76,155), AMBER=Color.rgb(180,118,20), BG=Color.rgb(246,248,251), TEXT=Color.rgb(27,38,49), MUTED=Color.rgb(82,96,109);
    private final TextRecognizer recognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    private ImageView preview;
    private TextView status, themeView;
    private EditText recognized, essay;
    private RadioGroup mode;
    private LinearLayout candidatesBox;
    private Bitmap baseBitmap;
    private File cameraFile;
    private Uri cameraUri;
    private final ArrayList<Candidate> candidates=new ArrayList<>();
    private final Random random=new Random();
    private String theme;
    private final String[] themes={
            "Os impactos do uso excessivo da tecnologia nos jovens brasileiros",
            "A importância do engajamento do jovem com questões sociais do seu tempo",
            "O papel da leitura na formação dos jovens",
            "Desafios para combater a desinformação entre adolescentes",
            "Como equilibrar tecnologia e concentração na vida escolar",
            "O papel da escola na formação de leitores críticos",
            "Caminhos para ampliar a participação dos jovens na comunidade",
            "Responsabilidade coletiva na preservação dos espaços públicos"
    };

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(NAVY);show();}
    private TextView tv(String s,int sp,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(TEXT);v.setPadding(10,9,10,9);v.setLineSpacing(2,1.08f);if(bold)v.setTypeface(null,1);return v;}
    private TextView muted(String s,int sp){TextView v=tv(s,sp,false);v.setTextColor(MUTED);return v;}
    private Button button(String s,int color,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));b.setMinHeight(54);b.setOnClickListener(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,5);b.setLayoutParams(p);return b;}
    private LinearLayout root(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(18,18,18,32);x.setBackgroundColor(BG);return x;}
    private ScrollView page(LinearLayout x){ScrollView s=new ScrollView(this);s.addView(x);return s;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(14,12,14,12);c.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,7,0,7);c.setLayoutParams(p);return c;}

    private void show(){
        theme=themes[random.nextInt(themes.length)];
        LinearLayout x=root();
        x.addView(button("← Voltar",NAVY,v->finish()));
        x.addView(tv("Redação v0.10",28,true));
        x.addView(muted("O OCR é um rascunho de leitura. Ele nunca recebe nota automaticamente e nunca substitui sua revisão.",14));
        LinearLayout t=card();t.addView(tv("Tema",16,true));themeView=tv(theme,19,true);t.addView(themeView);t.addView(button("Trocar tema",BLUE,v->{theme=themes[random.nextInt(themes.length)];themeView.setText(theme);}));x.addView(t);

        LinearLayout photo=card();
        photo.addView(tv("1. Ler uma foto",20,true));
        photo.addView(muted("Fotografe a folha de frente, com boa luz, sem sombra e ocupando a maior parte da imagem. O modo difícil tenta até 10 tratamentos diferentes.",14));
        mode=new RadioGroup(this);mode.setOrientation(RadioGroup.VERTICAL);
        RadioButton auto=new RadioButton(this),hard=new RadioButton(this);auto.setId(1);auto.setText("Leitura rápida");hard.setId(2);hard.setText("Caligrafia difícil — mais tentativas");mode.addView(auto);mode.addView(hard);mode.check(2);photo.addView(mode);
        photo.addView(button("Escolher foto em alta resolução",BLUE,v->pick()));
        photo.addView(button("Tirar foto em alta resolução",NAVY,v->camera()));
        preview=new ImageView(this);preview.setAdjustViewBounds(true);preview.setMaxHeight(650);photo.addView(preview,new LinearLayout.LayoutParams(-1,-2));
        status=muted("Nenhuma imagem enviada.",14);photo.addView(status);x.addView(photo);

        candidatesBox=card();candidatesBox.addView(tv("2. Comparar leituras",20,true));candidatesBox.addView(muted("Depois da leitura, as melhores versões aparecem aqui. Se o texto parecer fragmentado demais, a avaliação será bloqueada.",14));x.addView(candidatesBox);

        LinearLayout rec=card();rec.addView(tv("Texto reconhecido — revise antes de usar",19,true));recognized=new EditText(this);recognized.setMinLines(8);recognized.setGravity(Gravity.TOP);recognized.setTextSize(16);recognized.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);rec.addView(recognized,new LinearLayout.LayoutParams(-1,-2));rec.addView(button("Limpeza conservadora",GREEN,v->recognized.setText(cleanup(recognized.getText().toString()))));rec.addView(muted("A limpeza só organiza espaços, quebras, maiúsculas iniciais e pontuação muito básica. Ela não melhora seu argumento nem reescreve gramática.",13));rec.addView(button("Copiar texto revisado para a redação",PURPLE,v->copyReviewed()));x.addView(rec);

        LinearLayout ed=card();ed.addView(tv("3. Sua redação",20,true));essay=new EditText(this);essay.setMinLines(12);essay.setGravity(Gravity.TOP);essay.setTextSize(16);essay.setHint("Você também pode digitar a redação diretamente aqui.");essay.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);ed.addView(essay,new LinearLayout.LayoutParams(-1,-2));ed.addView(button("Avaliar como treino",PURPLE,v->scoreEssay()));x.addView(ed);
        setContentView(page(x));
    }

    private void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK);}
    private void camera(){
        try{
            File dir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);if(dir==null)dir=getCacheDir();
            String ts=new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
            cameraFile=File.createTempFile("essay_"+ts+"_",".jpg",dir);
            cameraUri= FileProvider.getUriForFile(this,getPackageName()+".fileprovider",cameraFile);
            Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,CAM);
        }catch(Exception e){Toast.makeText(this,"Não consegui abrir a câmera: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK)return;try{
        Bitmap b=null;
        if(requestCode==PICK&&data!=null&&data.getData()!=null){Uri u=data.getData();b=decodeUri(u,2800);b=rotateByExif(u,b);}
        else if(requestCode==CAM&&cameraFile!=null&&cameraFile.exists()){b=decodeFile(cameraFile,2800);b=rotateByExif(cameraFile,b);}
        if(b==null){status.setText("A imagem não pôde ser aberta.");return;}
        if(baseBitmap!=null&&!baseBitmap.isRecycled())baseBitmap.recycle();baseBitmap=b;preview.setImageBitmap(baseBitmap);startOcr(baseBitmap,mode.getCheckedRadioButtonId()==2);
    }catch(Exception e){status.setText("Falha ao abrir a imagem: "+e.getMessage());}}

    private Bitmap decodeUri(Uri uri,int max) throws IOException {try(InputStream in=getContentResolver().openInputStream(uri)){Bitmap b=BitmapFactory.decodeStream(in);return limit(b,max);}}
    private Bitmap decodeFile(File f,int max){Bitmap b=BitmapFactory.decodeFile(f.getAbsolutePath());return limit(b,max);}
    private Bitmap limit(Bitmap b,int max){if(b==null)return null;int w=b.getWidth(),h=b.getHeight();if(Math.max(w,h)<=max)return b;float s=max/(float)Math.max(w,h);Bitmap out=Bitmap.createScaledBitmap(b,Math.max(1,(int)(w*s)),Math.max(1,(int)(h*s)),true);if(out!=b)b.recycle();return out;}
    private Bitmap rotate(Bitmap b,int deg){if(b==null||deg==0)return b;Matrix m=new Matrix();m.postRotate(deg);Bitmap out=Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,true);if(out!=b)b.recycle();return out;}
    private Bitmap rotateByExif(Uri uri,Bitmap b){try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)return b;ExifInterface ex=new ExifInterface(in);return rotateExifValue(b,ex.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL));}catch(Exception e){return b;}}
    private Bitmap rotateByExif(File file,Bitmap b){try{ExifInterface ex=new ExifInterface(file);return rotateExifValue(b,ex.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL));}catch(Exception e){return b;}}
    private Bitmap rotateExifValue(Bitmap b,int o){if(o==ExifInterface.ORIENTATION_ROTATE_90)return rotate(b,90);if(o==ExifInterface.ORIENTATION_ROTATE_180)return rotate(b,180);if(o==ExifInterface.ORIENTATION_ROTATE_270)return rotate(b,270);return b;}

    private void startOcr(Bitmap b,boolean hard){
        status.setText(hard?"Fazendo até 10 leituras...":"Fazendo 3 leituras...");candidates.clear();candidatesBox.removeAllViews();candidatesBox.addView(tv("2. Comparar leituras",20,true));candidatesBox.addView(muted("Processando...",14));int count=hard?10:3;ocrNext(b,0,count);
    }
    private void ocrNext(Bitmap base,int index,int total){
        if(index>=total){finishOcr();return;}
        Bitmap v=variant(base,index);if(v==null){ocrNext(base,index+1,total);return;}
        recognizer.process(InputImage.fromBitmap(v,0)).addOnSuccessListener(t->{String text=t.getText()==null?"":t.getText();candidates.add(new Candidate(text,plausibility(text),index));if(v!=base&&!v.isRecycled())v.recycle();status.setText("Leitura "+(index+1)+" / "+total);ocrNext(base,index+1,total);}).addOnFailureListener(e->{if(v!=base&&!v.isRecycled())v.recycle();ocrNext(base,index+1,total);});
    }
    private Bitmap variant(Bitmap src,int i){
        if(i==0)return src;
        if(i==1)return grayscaleContrast(src,1.25f,-5,false,false);
        if(i==2)return grayscaleContrast(src,1.65f,-12,false,false);
        if(i==3)return grayscaleContrast(src,1.9f,-18,false,false);
        if(i==4)return threshold(src,135,false);
        if(i==5)return threshold(src,165,false);
        if(i==6)return threshold(src,195,false);
        if(i==7)return threshold(src,165,true);
        if(i==8)return rotateCopy(src,-2f);
        if(i==9)return rotateCopy(src,2f);
        return src;
    }
    private Bitmap rotateCopy(Bitmap src,float deg){Matrix m=new Matrix();m.postRotate(deg);return Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),m,true);}
    private Bitmap grayscaleContrast(Bitmap src,float factor,int offset,boolean thr,boolean invert){int w=src.getWidth(),h=src.getHeight();Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);int[] p=new int[w*h];src.getPixels(p,0,w,0,0,w,h);for(int i=0;i<p.length;i++){int c=p[i];int g=(int)(0.299*Color.red(c)+0.587*Color.green(c)+0.114*Color.blue(c));g=Math.round((g-128)*factor+128+offset);g=Math.max(0,Math.min(255,g));if(thr)g=g>165?255:0;if(invert)g=255-g;p[i]=Color.rgb(g,g,g);}out.setPixels(p,0,w,0,0,w,h);return out;}
    private Bitmap threshold(Bitmap src,int cut,boolean invert){int w=src.getWidth(),h=src.getHeight();Bitmap out=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);int[] p=new int[w*h];src.getPixels(p,0,w,0,0,w,h);for(int i=0;i<p.length;i++){int c=p[i];int g=(int)(0.299*Color.red(c)+0.587*Color.green(c)+0.114*Color.blue(c));g=g>cut?255:0;if(invert)g=255-g;p[i]=Color.rgb(g,g,g);}out.setPixels(p,0,w,0,0,w,h);return out;}

    private void finishOcr(){
        Collections.sort(candidates,(a,b)->Integer.compare(b.score,a.score));candidatesBox.removeAllViews();candidatesBox.addView(tv("2. Comparar leituras",20,true));
        if(candidates.isEmpty()||candidates.get(0).text.trim().isEmpty()){status.setText("Não encontrei texto legível. Tente outra foto com mais luz e enquadramento reto.");candidatesBox.addView(tv("Nenhuma leitura útil.",15,false));return;}
        int show=Math.min(3,candidates.size());
        for(int i=0;i<show;i++){final Candidate c=candidates.get(i);String quality=c.score>=70?"boa":c.score>=48?"revisar":"fraca";candidatesBox.addView(button("Usar versão "+(i+1)+" — qualidade "+quality+" ("+c.score+"/100)",c.score>=70?GREEN:(c.score>=48?AMBER:RED),v->recognized.setText(c.text)));}
        Candidate best=candidates.get(0);recognized.setText(best.text);
        if(best.score<48)status.setText("A melhor leitura parece fragmentada. Não vou tratar isso como redação pronta. Revise manualmente ou tire outra foto.");
        else if(best.score<70)status.setText("Leitura parcial. Compare as versões e revise palavras, espaços e parágrafos antes de copiar.");
        else status.setText("Leitura com boa estrutura aparente. Mesmo assim, compare com a folha antes de copiar.");
    }

    private int plausibility(String text){
        if(text==null)return 0;String s=text.trim();if(s.length()<20)return Math.max(0,s.length());
        String[] raw=s.split("\\s+");int words=0,one=0,longBad=0,letters=0,vowels=0,pt=0;
        HashSet<String> common=new HashSet<>(Arrays.asList("a","o","e","de","do","da","dos","das","em","um","uma","que","para","por","com","os","as","se","não","nao","no","na","como","mais","é","ser","ao","aos","sua","seu","isso","esse","essa"));
        for(String w0:raw){String w=w0.replaceAll("[^A-Za-zÀ-ÖØ-öø-ÿ]","");if(w.isEmpty())continue;words++;if(w.length()==1)one++;if(w.length()>19)longBad++;if(common.contains(w.toLowerCase(Locale.ROOT)))pt++;for(char c:w.toLowerCase(Locale.ROOT).toCharArray()){if(Character.isLetter(c)){letters++;if("aeiouáàâãéêíóôõúü".indexOf(c)>=0)vowels++;}}}
        if(words==0)return 0;double oneRatio=one/(double)words,longRatio=longBad/(double)words,vowelRatio=letters==0?0:vowels/(double)letters;int score=20;
        score+=Math.min(25,words/3);if(s.length()>180)score+=10;if(s.length()>500)score+=5;if(oneRatio<0.18)score+=12;else if(oneRatio>0.35)score-=18;if(longRatio<0.05)score+=8;else score-=12;if(vowelRatio>0.30&&vowelRatio<0.58)score+=12;else score-=12;score+=Math.min(12,pt*2);if(s.contains(".")||s.contains(","))score+=4;if(s.contains("\n"))score+=2;return Math.max(0,Math.min(100,score));
    }

    private void copyReviewed(){String s=recognized.getText().toString().trim();int p=plausibility(s);if(s.isEmpty()){Toast.makeText(this,"Não há texto reconhecido.",Toast.LENGTH_SHORT).show();return;}if(p<45){new android.app.AlertDialog.Builder(this).setTitle("A leitura ainda parece quebrada").setMessage("O texto reconhecido contém sinais de OCR ruim. Compare com a folha e corrija as palavras antes de copiar. Isso evita dar nota para um texto que o leitor inventou.").setPositiveButton("Continuar revisando",null).show();return;}essay.setText(s);Toast.makeText(this,"Copiado. Agora confira uma última vez antes de avaliar.",Toast.LENGTH_LONG).show();}

    private String cleanup(String raw){
        if(raw==null)return "";String s=raw.replace('\u00A0',' ').replaceAll("[ \\t]+"," ").replaceAll(" *\\n *","\n").replaceAll("\\n{3,}","\n\n").trim();
        String[] paras=s.split("\\n\\n+");StringBuilder out=new StringBuilder();for(String p:paras){String one=p.replaceAll("(?<![.!?;:])\\n(?=\\p{Ll})"," ").replaceAll(" +"," ").trim();if(one.isEmpty())continue;one=capitalizeSentences(one);if(!one.matches(".*[.!?]$"))one+=".";if(out.length()>0)out.append("\n\n");out.append(one);}return out.toString();
    }
    private String capitalizeSentences(String s){StringBuilder b=new StringBuilder(s);boolean cap=true;for(int i=0;i<b.length();i++){char c=b.charAt(i);if(cap&&Character.isLetter(c)){b.setCharAt(i,Character.toUpperCase(c));cap=false;}if(c=='.'||c=='!'||c=='?')cap=true;}return b.toString();}

    private void scoreEssay(){
        String s=essay.getText().toString().trim();if(s.length()<80){alert("Texto curto demais","Escreva a redação antes de avaliar. Um trecho pequeno de OCR não deve receber nota como se fosse uma redação completa.");return;}int plaus=plausibility(s);if(plaus<45){alert("Texto com sinais de leitura corrompida","A estrutura parece fragmentada demais para uma avaliação confiável. Revise o OCR ou digite o texto manualmente.");return;}
        String[] words=s.split("\\s+");int wc=words.length;String[] paras=s.split("\\n\\s*\\n");int pc=0;for(String p:paras)if(p.trim().length()>20)pc++;
        int sentences=Math.max(1,s.split("[.!?]+").length);int commas=countChar(s,',');
        int connectors=countWords(s,new String[]{"portanto","porém","porem","contudo","entretanto","além","alem","assim","porque","pois","desse modo","dessa forma","por outro lado","em primeiro lugar","além disso","alem disso","logo"});
        int themeHits=themeHits(s,theme);
        int dev=clamp(wc>=180?20:wc>=140?18:wc>=100?15:wc>=70?11:7,0,20);
        int org=clamp((pc>=3?10:pc==2?7:4)+(sentences>=5?5:3)+(wc>90?5:2),0,20);
        int coh=clamp(8+Math.min(8,connectors*2)+(commas>1?2:0)+(pc>=2?2:0),0,20);
        int lang=clamp(10+(plaus>=70?5:2)+(commas>0?2:0)+(sentences>=4?3:1),0,20);
        int themeScore=clamp(8+Math.min(8,themeHits*2)+(wc>=100?4:1),0,20);
        int total=dev+org+coh+lang+themeScore;
        LinearLayout x=root();x.addView(button("← Voltar à redação",NAVY,v->show()));x.addView(tv("Avaliação de treino",27,true));x.addView(muted("Estimativa automática para orientar estudo. Não é nota oficial do CEFET/RJ e não substitui correção humana.",14));
        LinearLayout c=card();c.addView(tv(total+" / 100",32,true));c.addView(tv("Desenvolvimento: "+dev+"/20",16,false));c.addView(tv("Organização: "+org+"/20",16,false));c.addView(tv("Coesão: "+coh+"/20",16,false));c.addView(tv("Linguagem/legibilidade digital: "+lang+"/20",16,false));c.addView(tv("Aderência aparente ao tema: "+themeScore+"/20",16,false));x.addView(c);
        LinearLayout data=card();data.addView(tv("O que o sistema observou",19,true));data.addView(tv(wc+" palavras • "+pc+" parágrafos • "+sentences+" períodos • "+connectors+" conectivos identificados",15,false));data.addView(tv(feedback(wc,pc,connectors,themeHits),15,false));x.addView(data);
        setContentView(page(x));
    }
    private int countChar(String s,char c){int n=0;for(int i=0;i<s.length();i++)if(s.charAt(i)==c)n++;return n;}
    private int countWords(String text,String[] arr){String low=text.toLowerCase(Locale.ROOT);int n=0;for(String w:arr)if(low.contains(w))n++;return n;}
    private int themeHits(String text,String th){String low=text.toLowerCase(Locale.ROOT),t=th.toLowerCase(Locale.ROOT).replaceAll("[^a-záàâãéêíóôõúç ]"," ");int n=0;for(String w:t.split(" +")){if(w.length()<5)continue;if(low.contains(w))n++;}return n;}
    private String feedback(int wc,int pc,int con,int themeHits){StringBuilder b=new StringBuilder();if(wc<120)b.append("• Desenvolva mais as ideias com explicação, causa, consequência ou exemplo.\n");else b.append("• O texto tem extensão suficiente para analisar desenvolvimento.\n");if(pc<3)b.append("• Tente separar introdução, desenvolvimento e fechamento em parágrafos claros.\n");else b.append("• A divisão em parágrafos está visível.\n");if(con<2)b.append("• Trabalhe ligações entre ideias; não basta colocar conectivos aleatórios, eles precisam indicar a relação correta.\n");else b.append("• Há sinais de articulação entre partes do texto.\n");if(themeHits<2)b.append("• Confira se cada parágrafo responde diretamente ao tema proposto.\n");else b.append("• O vocabulário indica relação com o tema, mas ainda é necessário verificar a qualidade dos argumentos.");return b.toString();}
    private int clamp(int v,int a,int b){return Math.max(a,Math.min(b,v));}
    private void alert(String title,String msg){new android.app.AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK",null).show();}

    @Override protected void onDestroy(){recognizer.close();if(baseBitmap!=null&&!baseBitmap.isRecycled())baseBitmap.recycle();super.onDestroy();}
    private static class Candidate{final String text;final int score,index;Candidate(String t,int s,int i){text=t;score=s;index=i;}}
}
