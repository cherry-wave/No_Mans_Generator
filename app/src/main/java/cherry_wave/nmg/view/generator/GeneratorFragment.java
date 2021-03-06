package cherry_wave.nmg.view.generator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import butterknife.BindView;
import cherry_wave.nmg.NMGFragment;
import cherry_wave.nmg.R;
import cherry_wave.nmg.controller.PatternUtils;
import cherry_wave.nmg.controller.SyllableUtils;
import cherry_wave.nmg.model.Name;
import cherry_wave.nmg.model.Pattern;
import cherry_wave.nmg.model.Syllable;
import cherry_wave.nmg.view.InfoDialog;

public class GeneratorFragment extends NMGFragment implements SwipeRefreshLayout.OnRefreshListener, DiscreteSeekBar.OnProgressChangeListener {

    private static final String GENERATED_NAMES_AMOUNT = "generatedNamesAmount";

    private SharedPreferences.Editor editor;

    private Integer generatedNamesAmount;

    @BindView(R.id.generator_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.generator_list)
    ListView names;
    @BindView(R.id.generator_empty_state)
    TextView generatorEmptyState;
    @BindView(R.id.generated_names_amount_selector)
    DiscreteSeekBar amountSelector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflate(inflater, R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        amountSelector.setOnProgressChangeListener(this);

        setProgress();
    }

    private void setProgress() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        generatedNamesAmount = sharedPreferences.getInt(GENERATED_NAMES_AMOUNT, 10);
        amountSelector.setProgress(generatedNamesAmount);
    }

    @Override
    public void onRefresh() {
        List<Pattern> activePatterns = PatternUtils.getActivePatterns();
        boolean patternsContainConsonantStart = PatternUtils.containsAnyOrStart(activePatterns, PatternUtils.Start.CONSONANT);
        boolean patternsContainVowelStart = PatternUtils.containsAnyOrStart(activePatterns, PatternUtils.Start.VOWEL);

        List<Syllable> consonantSyllables = SyllableUtils.getActiveSyllables(false);
        List<Syllable> vowelSyllables = SyllableUtils.getActiveSyllables(true);

        if (consonantSyllables.isEmpty() && vowelSyllables.isEmpty()) {
            InfoDialog.newInstance(R.string.generator_info_no_syllables).show(getFragmentManager(), InfoDialog.class.getCanonicalName());
            return;
        } else if (activePatterns.isEmpty()) {
            InfoDialog.newInstance(R.string.generator_info_no_patterns).show(getFragmentManager(), InfoDialog.class.getCanonicalName());
            return;
        } else if (consonantSyllables.isEmpty()) {
            if (!patternsContainVowelStart) {
                InfoDialog.newInstance(R.string.generator_info_no_consonant_syllables).show(getFragmentManager(), InfoDialog.class.getCanonicalName());
                return;
            }
            Iterator<Pattern> iterator = activePatterns.iterator();
            while (iterator.hasNext()) {
                Pattern pattern = iterator.next();
                if (PatternUtils.containsOnlyStart(pattern, PatternUtils.Start.CONSONANT)) {
                    iterator.remove();
                }
            }
        } else if (vowelSyllables.isEmpty()) {
            if (!patternsContainConsonantStart) {
                InfoDialog.newInstance(R.string.generator_info_no_vowel_syllables).show(getFragmentManager(), InfoDialog.class.getCanonicalName());
                return;
            }
            Iterator<Pattern> iterator = activePatterns.iterator();
            while (iterator.hasNext()) {
                Pattern pattern = iterator.next();
                if (PatternUtils.containsOnlyStart(pattern, PatternUtils.Start.VOWEL)) {
                    iterator.remove();
                }
            }
        }
        SortedSet<Name> generatedNames = new TreeSet<>(new Comparator<Name>() {
            @Override
            public int compare(Name name1, Name name2) {
                return name1.getCharacters().compareTo(name2.getCharacters());
            }
        });
        Random anyRandom = new Random();
        Random consonantRandom = new Random();
        Random vowelRandom = new Random();

        for (int i = 1; i <= generatedNamesAmount; i++) {
            Pattern pattern = activePatterns.get((int) (Math.random() * activePatterns.size()));
            String characters = pattern.getCharacters();
            String[] subPatterns = characters.split("\\{");
            StringBuilder name = new StringBuilder();
            for (String subPattern : subPatterns) {
                int indexOfClose = subPattern.indexOf('}');
                if (indexOfClose == -1) {
                    name.append(subPattern);
                    continue;
                }
                String append = subPattern.substring(indexOfClose + 1);
                subPattern = subPattern.substring(0, indexOfClose);

                // set together the starting syllable
                String startSyllable;
                if (vowelSyllables.isEmpty() || PatternUtils.startsWith(subPattern, PatternUtils.Start.CONSONANT)) {
                    startSyllable = consonantSyllables.get(consonantRandom.nextInt(consonantSyllables.size())).getCharacters();
                } else if (consonantSyllables.isEmpty() || PatternUtils.startsWith(subPattern, PatternUtils.Start.VOWEL)) {
                    startSyllable = vowelSyllables.get(vowelRandom.nextInt(vowelSyllables.size())).getCharacters();
                } else {
                    if (anyRandom.nextInt(1) == 0) {
                        startSyllable = consonantSyllables.get(consonantRandom.nextInt(consonantSyllables.size())).getCharacters();
                    } else {
                        startSyllable = vowelSyllables.get(vowelRandom.nextInt(vowelSyllables.size())).getCharacters();
                    }
                }
                if (PatternUtils.startsWithUppercase(subPattern)) {
                    startSyllable = startSyllable.substring(0, 1).toUpperCase() + startSyllable.substring(1);
                }
                name.append(startSyllable);

                // add following syllables
                int to = PatternUtils.getRangeTo(subPattern);
                for (int from = 1; from < to; from++) {
                    String syllable;
                    if (anyRandom.nextInt(1) == 0 || vowelSyllables.isEmpty()) {
                        if (consonantSyllables.isEmpty()) {
                            syllable = vowelSyllables.get(vowelRandom.nextInt(vowelSyllables.size())).getCharacters();
                        } else {
                            syllable = consonantSyllables.get(consonantRandom.nextInt(consonantSyllables.size())).getCharacters();
                        }
                    } else {
                        syllable = vowelSyllables.get(vowelRandom.nextInt(vowelSyllables.size())).getCharacters();
                    }
                    name.append(syllable);
                }

                // add non pattern content
                name.append(append);
            }
            generatedNames.add(new Name(name.toString()));
        }

        GeneratedNamesAdapter generatedNamesAdapter = new GeneratedNamesAdapter(getContext(), generatedNames.toArray(new Name[generatedNames.size()]));
        names.setAdapter(generatedNamesAdapter);

        generatorEmptyState.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        generatedNamesAmount = value;
        editor.putInt(GENERATED_NAMES_AMOUNT, generatedNamesAmount);
        editor.apply();
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }
}
