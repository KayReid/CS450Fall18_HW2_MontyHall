// citation: Matthijs suggested I use retainInstance (which you said to do during class, but I forgot about it)
// I also used http://code.hootsuite.com/orientation-changes-on-android/ to figure out what happened on orientation changes (because my notes where a little vague)
// Images: pikachu is from https://www.google.com/search?biw=1536&bih=747&tbm=isch&sa=1&ei=woO6W7bbNIGd_QbPi534Bw&q=pikachu&oq=pika&gs_l=img.3.0.0i67l3j0l4j0i67j0l2.52519.67784..69089...3.0..2.1213.3928.3j1j4-1j0j1j2......2....1..gws-wiz-img.....0..35i39.aWPa9RLn9sQ#imgrc=W-c8X-MHIaekGM:
// magikarp is from https://www.google.com/search?q=magikarp&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiOmMCvrPXdAhWqwFkKHavfDtUQ_AUIDigB&biw=1536&bih=747#imgrc=EME_P3jMK3N9QM:
// the numbers are from https://www.freeiconspng.com/img/11916

package edu.stlawu.montyhall;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static edu.stlawu.montyhall.MainFragment.NEW_CLICKED;
import static edu.stlawu.montyhall.MainFragment.PREF_NAME;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment {

    public GameFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public Random ran = new Random();

    public ImageButton door1 = null;
    public ImageButton door2 = null;
    public ImageButton door3 = null;

    //check to see if door selected
    private ImageButton pickedDoor = null;
    private ImageButton goatDoor = null;

    // textviews
    private TextView prompt = null;
    private TextView again = null;
    private TextView timesWon = null;
    private TextView timesLost = null;
    private TextView timesTotal = null;

    // get the wins, losses and index of selected/win door
    private int win;
    private int loss;
    private int pickedDoorNumber = -1;
    private int winningDoor = -1;
    private int goatDoorOne;
    private int goatDoorTwo;
    private int openedDoor = -1;
    private int choiceNum = 0;

    //sound stuff
    public AudioAttributes aa = null;
    private SoundPool soundPool = null;
    private int sound_car = 0;
    private int sound_goat = 0;
    private ImageButton doors[];

    private void saveGame() {
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("win_count", win).apply();
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("loss_count", loss).apply();
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("win_Door", winningDoor).apply();
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("selected_door", pickedDoorNumber).apply();
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("choice_number", choiceNum).apply();
    }

    private void setup(){
        //load saved data
        winningDoor = getActivity().getPreferences(MODE_PRIVATE).getInt("win_Door", -1);
        // check if there is a winning door
        if (winningDoor == -1) {
            generate_door_setup();
        }

        // check if there is a selected door
        pickedDoorNumber = getActivity().getPreferences(MODE_PRIVATE).getInt("selected_door", -1);
        if (pickedDoorNumber == -1) {
            pickedDoor = null;
        } else {
            pickedDoor = doors[pickedDoorNumber];
            pickedDoor.setImageResource(R.drawable.closed_door_chosen_new);
        }
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("selected_door", pickedDoorNumber).apply();

        // check if there is an opened door
        openedDoor = getActivity().getPreferences(MODE_PRIVATE).getInt("goat_door_opened", -1);
        if (openedDoor == -1) {
            goatDoor = null;
        } else {
            goatDoor = doors[openedDoor];
            goatDoor.setImageResource(R.drawable.goatnew);
        }
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("goat_door_opened", openedDoor).apply();

        // check if the again button is available
        choiceNum = getActivity().getPreferences(MODE_PRIVATE).getInt("choice_number", 0);
        if(choiceNum == 2) {
            doors[winningDoor].setImageResource(R.drawable.carnew);
            int lastDoor = ran.nextInt(3);
            while(lastDoor == openedDoor || lastDoor == winningDoor){
                lastDoor = ran.nextInt(3);
            }
            doors[lastDoor].setImageResource(R.drawable.goatnew);
            int check = getActivity().getPreferences(MODE_PRIVATE).getInt("choice_number", 0);
            if(check == 0){
                prompt.setText(R.string.you_won);
            }else{
                prompt.setText(R.string.you_lost);
            }
            again.setEnabled(true);
            again.setVisibility(View.VISIBLE);
        }else{
            if(choiceNum == 1){
                prompt.setText(R.string.switch_door);
            }
        }
    }

    private void resetGame(){
        pickedDoorNumber = -1;
        openedDoor = -1;
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("goat_door_opened", openedDoor).apply();
        pickedDoor = null;
        goatDoor = null;
        choiceNum = 0;
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("choice_number", choiceNum).apply();
        generate_door_setup();

        door1.setImageResource(R.drawable.closed_door_new);
        door2.setImageResource(R.drawable.closed_door_new);
        door3.setImageResource(R.drawable.closed_door_new);
        again.setVisibility(View.INVISIBLE);
        prompt.setText(R.string.choose_door);

        door1.setEnabled(true);
        door2.setEnabled(true);
        door3.setEnabled(true);
        saveGame();
    }

    private void setScore(){
        win = getActivity().getPreferences(MODE_PRIVATE).getInt("win_count", 0);
        loss = getActivity().getPreferences(MODE_PRIVATE).getInt("loss_count", 0);

        timesLost.setText(String.format("%d", loss));
        timesWon.setText(String.format("%d", win));
        timesTotal.setText(String.format("%d", win + loss));
    }

    private void doBounceAnimation(View targetView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "translationY", 0, -25, 0);

        animator.setStartDelay(250);
        animator.setDuration(1750);
        animator.start();
    }

    private void generate_door_setup(){
        winningDoor = ran.nextInt(3);
        goatDoorOne = ran.nextInt(3);
        while(winningDoor == goatDoorOne){
            goatDoorOne = ran.nextInt(3);
        }
        goatDoorTwo = ran.nextInt(3);
        while(winningDoor == goatDoorTwo || goatDoorOne == goatDoorTwo){
            goatDoorTwo = ran.nextInt(3);
        }
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("win_Door", winningDoor).apply();
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("goat_Door", goatDoorOne).apply();
        getActivity().getPreferences(MODE_PRIVATE).edit().putInt("goat_Door_Two", goatDoorTwo).apply();
    }

    // if a door is picked
    private void imageButtonClicked(final ImageButton aDoor) {
        // if a door has not been picked yet
        // TODO: check choiceNum somewhere
        choiceNum = getActivity().getPreferences(MODE_PRIVATE).getInt("choice_number", 0);

        if (pickedDoor == null && choiceNum == 0) {
            choiceNum++;
            getActivity().getPreferences(MODE_PRIVATE).edit().putInt("choice_number", choiceNum).apply();
            pickedDoor = aDoor;
            pickedDoorNumber = Arrays.asList(doors).indexOf(aDoor);
            getActivity().getPreferences(MODE_PRIVATE).edit().putInt("selected_door", pickedDoorNumber).apply();

            // change prompt
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    prompt.setText(R.string.switch_door);
                    aDoor.setImageResource(R.drawable.closed_door_chosen_new);

                    int goatDoorSetup = getActivity().getPreferences(MODE_PRIVATE).getInt("goat_Door", goatDoorOne);
                    if (goatDoorSetup == pickedDoorNumber){
                        goatDoorSetup = getActivity().getPreferences(MODE_PRIVATE).getInt("goat_Door_Two", goatDoorTwo);
                    }

                    // set up goat doors
                    ImageButton otherDoor = doors[goatDoorSetup];
                    otherDoor.setImageResource(R.drawable.goatnew);
                    openedDoor = goatDoorSetup;
                    goatDoor = otherDoor;
                    getActivity().getPreferences(MODE_PRIVATE).edit().putInt("goat_door_opened", openedDoor).apply();
                    doBounceAnimation(otherDoor);
                    soundPool.play(sound_goat, 1f, 1f, 1, 0, 1f);
                }
            }, 1000);
        }

        // second pick
        else {
            if(choiceNum == 1 && aDoor != goatDoor) {
                choiceNum++;
                int newPickedDoorNumber = Arrays.asList(doors).indexOf(aDoor);

                if(pickedDoorNumber != newPickedDoorNumber) {
                    pickedDoor = doors[pickedDoorNumber];
                    pickedDoor.setImageResource(R.drawable.closed_door_new);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        aDoor.setImageResource(R.drawable.three);
                    }
                }, 1000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        aDoor.setImageResource(R.drawable.two);
                    }
                }, 2000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        aDoor.setImageResource(R.drawable.one);
                    }
                }, 3000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (doors[winningDoor] == aDoor) {
                            door1.setEnabled(false);
                            door2.setEnabled(false);
                            door3.setEnabled(false);
                            aDoor.setImageResource(R.drawable.carnew);

                            int lastGoat = getActivity().getPreferences(MODE_PRIVATE).getInt("goat_Door_Two", goatDoorTwo);
                            if (lastGoat == openedDoor){
                                lastGoat = getActivity().getPreferences(MODE_PRIVATE).getInt("goat_Door", goatDoorOne);
                            }

                            ImageButton otherDoor = doors[lastGoat];
                            otherDoor.setImageResource(R.drawable.goatnew);

                            win++;
                            // save the last round as a win or lost in case of flip
                            getActivity().getPreferences(MODE_PRIVATE).edit().putInt("last_round", 0).apply();
                            prompt.setText(R.string.you_won);
                            soundPool.play(sound_car, 1f, 1f, 1, 0, 1f);
                            doBounceAnimation(aDoor);
                        } else {
                            door1.setEnabled(false);
                            door2.setEnabled(false);
                            door3.setEnabled(false);
                            aDoor.setImageResource(R.drawable.goatnew);
                            loss++;
                            // save the last round as a win or lost in case of flip
                            getActivity().getPreferences(MODE_PRIVATE).edit().putInt("last_round", 1).apply();
                            prompt.setText(R.string.you_lost);
                            doors[winningDoor].setImageResource(R.drawable.carnew);
                            soundPool.play(sound_goat, 1f, 1f, 1, 0, 1f);
                            doBounceAnimation(aDoor);
                        }

                        // reset
                        again.setEnabled(true);
                        again.setVisibility(View.VISIBLE);
                        saveGame();
                        setScore();
                    }
                }, 4000);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        //get wins and losses
        this.timesWon = view.findViewById(R.id.win_text);
        this.timesLost = view.findViewById(R.id.loss_text);
        this.timesTotal = view.findViewById(R.id.total_text);
        this.prompt = view.findViewById(R.id.prompt);
        this.again = view.findViewById(R.id.again);

        again.setVisibility(View.INVISIBLE);

        this.aa = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aa).build();
        this.sound_car = this.soundPool.load(getActivity(), R.raw.car_noise, 1);
        this.sound_goat = this.soundPool.load(getActivity(), R.raw.goat_noise, 1);

        door1 = view.findViewById(R.id.door1);
        door2 = view.findViewById(R.id.door2);
        door3 = view.findViewById(R.id.door3);

        // array with location of door strings
        doors = new ImageButton[]{door1, door2, door3};

        // put up score and setup
        setScore();

        // new game
        boolean newGame = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(NEW_CLICKED, true);
        SharedPreferences.Editor pref_ed = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        if(newGame){
            resetGame();
            setScore();
            pref_ed.putBoolean(NEW_CLICKED, false).apply();
            Log.e("Message", "Called newGame");
        }

        // always setup
        setup();

        // if orientation changes
        if(savedInstanceState != null){
            Log.e("Message", "Flipped");
        }

        this.door1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                imageButtonClicked(door1);
            }
        });

        this.door2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                imageButtonClicked(door2);
            }
        });

        this.door3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                imageButtonClicked(door3);
            }
        });

        // restart game
        again.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resetGame();
                setup();
            }
        });
        setRetainInstance(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        saveGame();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveGame();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveGame();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveGame();
    }
}

