package com.example.word_test_80;

import android.speech.tts.TextToSpeech;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class MainActivity extends AppCompatActivity{

    private TextView print_out;

    //    //read in
    private File word_dir;
    //    int just_above_four = 0;  //true = 1 , false = 0
//    int today_fin_to_again = 0;  //true = 1 , false = 0
//    int is_today_num_cou = 0;   // today remain number didn't review yet
    //
//    //1
//    final int before_howmany_day = 1;
//    final int test_number = 10;
////    final boolean sound = true;
//
    final int DAYS[] = {0,0,1,1,1,3,5,7,14,21,31,31,45};
    ////    final int learned = 100;
//    //2
////    final boolean delete_space = true;
////    final boolean COVER = false;
//    //3
//
//    //end
//    final boolean order = false;
    final int print_to = 11;
////	static int freq_final;
//
//    //file name
    ArrayList<String> all_file_dir;
//    String save_dir = null;
//    String file_name[] = null;
//    String file_name_end[] = {"gre","math","toelf"};
//    //global var
//    List <List<Words>> subject_words ;
//    List <Map<String, Integer>> subject_words_map;
    List <Words> today_word[] ;
    List <Words> not_today;
    //    int yesterday_count = 0;
    int cou[] = new int[DAYS.length] ;
    int remain = 0;
//    //Date
//    Date before_howmany_day_date = null;
//    Date yester_date = null;
    Date today_date;  // means today's 00:00
    //
//    //
    final Calendar calendar = new GregorianCalendar();
    //
//    Words now_word = null;
//    Thread thread_for_check;
//
//    TextView eng,chinese,connection;
    Button know,dont_know,easy;

    //user_ans
    int ran_num;
    Words ran_word;
    boolean explore_ans = false;
    static Thread ans_thread;

    //sound
    private TextToSpeech textToSpeech;
    public void speak(String s){
        int speechStatus = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }


    private Handler myHandler = new Handler()
    {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {

            if(msg.what == 0x1){
                print_out.setText(print_string);
            }
        }
    };

    String print_string = "";
    void print(String s){
        print_string = s;
        myHandler.sendEmptyMessage(0x1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        read_all_file();

        test_process();
    }

    public void init(){
        sound_init();
        view_init();

        dir_path_init();

        array_list_init();
        time_init();
        fre_init();

    }

    public void sound_init(){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void view_init(){
        print_out = findViewById(R.id.print_out);

        know = findViewById(R.id.know);
        know.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(explore_ans) {
                    Log.i("ppp-test_process", "know the answer");
                    if(!today_word[ran_word.status].remove(ran_word)){
                        if(!today_word[ran_word.status+3].remove(ran_word)){
                            if(!today_word[today_word.length-1].remove(ran_word)){
                                Toast.makeText(MainActivity.this,"can not remove this word" + ran_word.eng,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    //correct
                    if (ran_word.status >= DAYS.length - 1) {
                        // up to top
                        ran_word.status = DAYS.length - 1;
                        calendar.setTime(new Date());
                        Random ran = new Random();
                        calendar.add(calendar.DATE, DAYS[ran_word.status] + ran.nextInt(60));
                        ran_word.date = calendar.getTime();
                    } else {
                        ran_word.status++;
                        calendar.setTime(new Date());
                        calendar.add(calendar.DATE, DAYS[ran_word.status]);
                        ran_word.date = calendar.getTime();
                    }
                    not_today.add(ran_word);

                    LockSupport.unpark(ans_thread);
                }
            }
        });
        dont_know = findViewById(R.id.dont_know);
        dont_know.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (explore_ans) {
                    Log.i("ppp-test_process", "dont know the answer");

                    if(!today_word[ran_word.status].remove(ran_word)){
                        if(!today_word[ran_word.status+3].remove(ran_word)){
                            if(!today_word[today_word.length-1].remove(ran_word)){
                                Toast.makeText(MainActivity.this,"can not remove this word" + ran_word.eng,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    // not correct

                    ran_word.status = ran_word.status - 1;
                    if (ran_word.status < 0) {
                        ran_word.status = 0;
                    }
                    ran_word.date = new Date();

                    not_today.add(ran_word);
                    LockSupport.unpark(ans_thread);
                }
            }
        });

        easy = findViewById(R.id.easy);
        easy.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(explore_ans) {
                    Log.i("ppp-test_process", "too easy");

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("too easy?")//設定視窗標題
                            .setIcon(R.mipmap.ic_launcher)//設定對話視窗圖示
                            .setMessage("really want to delete \"" + ran_word.eng +"\" ?")//設定顯示的文字
                            .setPositiveButton("too easy",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("too_easy","remove " + ran_word.eng);
                                    //remove
                                    if(!today_word[ran_word.status].remove(ran_word)) {
                                        if (!today_word[ran_word.status + 3].remove(ran_word)) {
                                            if (!today_word[today_word.length - 1].remove(ran_word)) {
                                                Toast.makeText(MainActivity.this, "can not remove this word" + ran_word.eng, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    ran_word.status = DAYS.length - 1;
                                    // status to top
                                    calendar.setTime(new Date());
                                    calendar.add(calendar.DATE, 365);
                                    ran_word.date = calendar.getTime();

                                    not_today.add(ran_word);

                                    Log.i("too_easy","finish remove " + ran_word.eng);
                                    LockSupport.unpark(ans_thread);
                                }
                            })//設定結束的子視窗
                            .setNegativeButton("don't remove",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i("too_easy","don't remove");
//                                    // add back
//                                    not_today.add(ran_word);
                                    LockSupport.unpark(ans_thread);
                                }
                            })//設定結束的子視窗
                            .show();//呈現對話視窗

                }
            }
        });
    }

    public void show_the_ans(){
        if(ran_word.association.equals("no")) {
            print(ran_word.eng + "\n " + ran_word.chi + "\n ");
        }else{
            print(ran_word.eng + "\n " + ran_word.chi + "\n " + ran_word.association + "\n ");
        }
        speak(ran_word.eng);
    }

    public void dir_path_init() {
        String path = Environment.getExternalStorageDirectory() + "/DCIM/word/";

        word_dir = new File(path);
        Log.i("ppp-init-get_dir_path","dir : "+path);
        // create_folder_if_there_isnt
        if (!word_dir.exists()) {
            Log.i("ppp-init-get_dir_path","dir doesn't exists");
            System.exit(0);
            MainActivity.this.onDestroy();
        }
    } //end get_dir_path

    public void array_list_init() {
        class dont_use extends ArrayList<Words> {}
        Log.i("ppp-array_list_init"," DAYS.length : "+DAYS.length);
        today_word =new dont_use[DAYS.length] ;  // 0~ (DAYS.length-1)
        for(int i = 0 ; i<DAYS.length; i++){
            today_word[i] = new dont_use();
        }
        not_today = new dont_use();

        all_file_dir = new ArrayList<>();
    }

    public void time_init() {

    }

    public void fre_init() {
        today_date = new Date();
    }
    //read the file under specific dir
    public void read_all_file() {
        int file_num = 0;
        Log.i("ppp-read_all_file","word_dir : "+word_dir.toString());
        for (String fileName : word_dir.list()) {   // word_dir.list() have the file name
            String this_file = word_dir.toString() + "/" + fileName;
            all_file_dir.add(this_file);
            Log.i("ppp-read_all_file","this_file : "+this_file);
            if (fileName.contains("txt")) {  // check if is .txt
                read_this_file(this_file,file_num);
            }
            file_num++;
        }

        for(int i = 0;i<today_word.length;i++){
            Log.i("ppp-read_all_file",i + " : " + today_word[i].size());
        }
        Log.i("ppp-read_all_file", "not_today : " + not_today.size());
    }// end read_all_file

    public void read_this_file(String this_file,int file_num){
//            List<Words> words = new ArrayList<>();
//            Map<String, Integer> words_map = new HashMap<>();
        try {
            File f = new File(this_file);
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(f), "utf-8");
            BufferedReader br = new BufferedReader(read);
            for (int line = 0;br.ready();line++) {
                String pass = br.readLine();
                String pass2[] = pass.split("\t");

                System.out.println(pass2[2]);

                Words pass_word = null;
                try {
                    pass_word = new Words(new Date(pass2[0]),Integer.valueOf(pass2[1]),pass2[2],pass2[3],pass2[4],line,file_num);
                } catch (Exception e) {
                    Log.i("ppp-read_this_file","0 : "+pass2[0]);
                    Log.i("ppp-read_this_file","1 : "+pass2[1]);
                    Log.i("ppp-read_this_file","2 : "+pass2[2]);
                    Log.i("ppp-read_this_file","3 : "+pass2[3]);
                    Log.i("ppp-read_this_file","4 : "+pass2[4]);
                    e.printStackTrace();
                    Thread.sleep(5000);
                }

//                    words.add(pass_word);
//                    words_map.put(pass2[2], line);

                if(!pass_word.date.after(new Date())){
//                    if(this_file.contains("toeic")){
//                        if(pass_word.status + 3 >= today_word.length-1){
//                            today_word[today_word.length-1].add(pass_word);
//                        }else {
//                            today_word[pass_word.status + 3].add(pass_word);
//                        }
//                    }else{
                        today_word[pass_word.status].add(pass_word);
//                    }
                }else{
                    not_today.add(pass_word);
                }
            }
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }



    public void test_process(){
        new Thread( new Runnable() {
            @Override
            public void run() {
                ans_thread = Thread.currentThread();
                for(int now_status = DAYS.length-1; now_status >= 0 ; now_status--){
                    while(today_word[now_status].size()!=0){
                        Random ran = new Random();
                        ran_num =ran.nextInt(today_word[now_status].size());
                        ran_word = today_word[now_status].get(ran_num);
                        print(ran_word.eng);
                        explore_ans = false;
                        LockSupport.park();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        write_in();
        super.onPause();
    }

    public void write_in(){
        FileWriter fw[] = new FileWriter[all_file_dir.size()];
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
        for(int i = 0;i<all_file_dir.size();i++) {
            try {
                fw[i] = new FileWriter(all_file_dir.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < today_word.length; i++) {
            for(Words pass : today_word[i]) {
                write(fw,pass,sdFormat);
            }
        }
        for(Words pass : not_today) {
            write(fw,pass,sdFormat);
        }

        for(FileWriter f:fw) {
            try {
                f.flush();
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void write(FileWriter fw[],Words pass,SimpleDateFormat sdFormat){
        String strDate = sdFormat.format(pass.date);
        try {
            fw[pass.which_file].write(strDate + "\t" + pass.status + "\t" + pass.eng + "\t" + pass.chi + "\t" + pass.association  + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ((!pass.date.after(new Date()))) {
            remain++;
        }

        if (pass.status >= print_to) {
            cou[print_to]++;
        } else {
            cou[pass.status]++;
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            explore_ans = true;
            show_the_ans();
        }
        return super.onTouchEvent(event);
    }

}
