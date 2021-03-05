package com.saurav.quizme;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.saurav.quizme.databinding.ActivityQuizBinding;

import java.util.ArrayList;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {
    ActivityQuizBinding binding;
    ArrayList<Question> questions;
    int index=0;
    Question question;
    CountDownTimer timer;
    FirebaseFirestore database;
    ProgressDialog dialog,dialog1;
    int correctAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        questions=new ArrayList<>();
        database=FirebaseFirestore.getInstance();

        dialog=new ProgressDialog(this);
        dialog.setMessage("Processing ur Result...");

        dialog1=new ProgressDialog(this);
        dialog1.setMessage("Please Wait...");



        Random random=new Random();
        int rand = random.nextInt(6);

        String catId= getIntent().getStringExtra("catId");
        dialog1.show();
        database.collection("categories")
                .document(catId)
                .collection("Questions")
                .whereGreaterThanOrEqualTo("index",rand)
                .orderBy("index")
                .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
         dialog1.dismiss();
              if(queryDocumentSnapshots.getDocuments().size()<5){
                  database.collection("categories")
                          .document(catId)
                          .collection("Questions")
                          .whereLessThanOrEqualTo("index",rand)
                          .orderBy("index")
                          .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                      @Override
                      public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                              for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                                  Question question = snapshot.toObject(Question.class);
                                  questions.add(question);
                              }
                              setNextQuestion();

                      }
                  });

              }
              else {
                  for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                      Question question = snapshot.toObject(Question.class);
                      questions.add(question);
                  }
                  setNextQuestion();
              }
            }
        });


resetTimer();
    setNextQuestion();
    }
    void resetTimer(){
        timer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.timer.setText(String.valueOf(millisUntilFinished/1000));

            }

            @Override
            public void onFinish() {

            }
        };
    }

    void showAnswer(){
        if(question.getAnswer().equals(binding.option1.getText().toString()))
            binding.option1.setBackground(getResources().getDrawable(R.drawable.option_right));
       else if(question.getAnswer().equals(binding.option2.getText().toString()))
            binding.option2.setBackground(getResources().getDrawable(R.drawable.option_right));
       else if(question.getAnswer().equals(binding.option3.getText().toString()))
            binding.option3.setBackground(getResources().getDrawable(R.drawable.option_right));
       else if(question.getAnswer().equals(binding.option4.getText().toString()))
            binding.option4.setBackground(getResources().getDrawable(R.drawable.option_right));


    }

    void setNextQuestion(){
        if (timer!=null)
            timer.cancel();
        timer.start();
        if (index < questions.size()){
            binding.questionCounter.setText(String.format("%d/%d",(index+1),questions.size()));
             question = questions.get(index);
            binding.question.setText(question.getQuestion());
            binding.option1.setText(question.getOption1());
            binding.option2.setText(question.getOption2());
            binding.option3.setText(question.getOption3());
            binding.option4.setText(question.getOption4());



        }
    }
    public void checkAnswer(TextView textView){
       String selectAnswer = textView.getText().toString();
       if (selectAnswer.equals(question.getAnswer())){
           correctAnswers++;
           textView.setBackground(getResources().getDrawable(R.drawable.option_right));
       }else {
           showAnswer();
           textView.setBackground(getResources().getDrawable(R.drawable.option_wrong));
       }
    }
    void reset(){
        binding.option1.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option2.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option3.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option4.setBackground(getResources().getDrawable(R.drawable.option_unselected));

    }
    public void onclick(View view){
        switch (view.getId()){
            case R.id.option_1:
            case R.id.option_2:
            case R.id.option_3:
            case R.id.option_4:
                if (timer!=null)
                    timer.cancel();
                TextView selected = (TextView) view;
                checkAnswer(selected);
                break;

            case R.id.next_btn:
                reset();
                if (index <=questions.size()) {
                    index++;
                    setNextQuestion();
                }else {
                    dialog.show();

                    Intent intent = new Intent(QuizActivity.this,ResultActivity.class);
                    intent.putExtra("correct",correctAnswers);
                    intent.putExtra("total",questions.size());
                    startActivity(intent);
                    finish();
                }
                break;
                
            default:
                Toast.makeText(this, "Please select a field", Toast.LENGTH_SHORT).show();
                    
        }
    }

}