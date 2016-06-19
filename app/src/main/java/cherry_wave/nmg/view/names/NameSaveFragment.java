package cherry_wave.nmg.view.names;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.orm.SugarRecord;

import org.parceler.Parcels;

import cherry_wave.nmg.NMGDialogFragment;
import cherry_wave.nmg.R;
import cherry_wave.nmg.model.Name;

public class NameSaveFragment extends NMGDialogFragment {

    private static final String ARG_NAME = "name";

    private NamesFragment namesFragment;
    private Name name;

    public static NameSaveFragment newInstance(Name name) {
        NameSaveFragment nameSaveDialog = new NameSaveFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_NAME, Parcels.wrap(name));
        nameSaveDialog.setArguments(args);
        return nameSaveDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        namesFragment = (NamesFragment) getTargetFragment();
        name = Parcels.unwrap(getArguments().getParcelable(ARG_NAME));
        if (name == null) {
            name = new Name("");
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(R.string.title_save_name)
                .autoDismiss(false)
                .positiveText(R.string.save)
                .positiveColorRes(R.color.colorPrimary)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText editText = dialog.getInputEditText();
                        name.setCharacters(editText.getText().toString());
                        save();
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.colorPrimary)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(null, name.getCharacters(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                });
        return builder.build();
    }

    private void save() {
        SugarRecord.save(name);
        namesFragment.updateNames();
    }

}
