package cherry_wave.nmg;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class NMGFragment extends Fragment {

    protected View inflate(LayoutInflater inflater, int resource, ViewGroup root, boolean attachToRoot) {
        View view = inflater.inflate(resource, root, attachToRoot);
        ButterKnife.bind(this, view);
        return view;
    }

}
