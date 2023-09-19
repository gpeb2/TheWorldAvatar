package uk.ac.cam.cares.jps.addasset;

import static uk.ac.cam.cares.jps.utils.AssetInfoConstant.MANUAL_SECTION_TITLE;
import static uk.ac.cam.cares.jps.utils.AssetInfoConstant.SPEC_SHEET_SECTION_TITLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import uk.ac.cam.cares.jps.addasset.model.AddAssetViewModel;
import uk.ac.cam.cares.jps.addasset.model.AssetPropertyDataModel;
import uk.ac.cam.cares.jps.addasset.model.DropDownDataModel;
import uk.ac.cam.cares.jps.addasset.view.DataSheetItemView;
import uk.ac.cam.cares.jps.addasset.view.PropertyAutoCompleteTextView;
import uk.ac.cam.cares.jps.addasset.view.PropertyBaseInputTextView;
import uk.ac.cam.cares.jps.addasset.view.PropertyGeneralInputTextView;

@AndroidEntryPoint
public class TabFragment extends Fragment {
    AddAssetViewModel viewModel;
    List<String> sections;
    private Logger LOGGER = Logger.getLogger(TabFragment.class);

    TabFragment(List<String> sections) {
        this.sections = sections;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BasicConfigurator.configure();
        ScrollView scrollView = new ScrollView(inflater.getContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout root = new LinearLayout(inflater.getContext());
        root.setOrientation(LinearLayout.VERTICAL);

        viewModel = new ViewModelProvider(requireActivity()).get(AddAssetViewModel.class);

        for (String section : sections) {
            View sectionView;
            if (Arrays.asList(SPEC_SHEET_SECTION_TITLE, MANUAL_SECTION_TITLE).contains(section)) {
                sectionView = createDataSheetSection(inflater, section);
            } else {
                sectionView = createInputTextSection(inflater, section);
            }
            root.addView(sectionView);
        }

        scrollView.addView(root);
        return scrollView;
    }

    private View createInputTextSection(LayoutInflater inflater, String section) {
        View sectionView = inflater.inflate(R.layout.view_input_text_section, null);
        ((TextView) sectionView.findViewById(R.id.section_label)).setText(section);

        LinearLayout linearLayout = sectionView.findViewById(R.id.linear_layout);
        for (String fieldName : viewModel.getInputFieldNamesBySection().get(section)) {
            AssetPropertyDataModel property = viewModel.getInputFieldModels().get(fieldName);
            PropertyBaseInputTextView inputText;
            if (property instanceof DropDownDataModel) {
                inputText = new PropertyAutoCompleteTextView(requireContext(), (DropDownDataModel) property);
                ((DropDownDataModel) property).getMutableLabelsToIri().observe(this.getViewLifecycleOwner(), labelsToIriMap -> {
                    ((PropertyAutoCompleteTextView) inputText).updateAdapterList(((DropDownDataModel) property).getOrderedOptionList());
                });
            } else {
                inputText = new PropertyGeneralInputTextView(requireContext(), property);
            }
            property.getIsMissingField().observe(this.getViewLifecycleOwner(), isMissing -> {
                if (isMissing) {
                    inputText.setInputLayoutError(getResources().getText(R.string.field_is_required));
                } else {
                    inputText.setInputLayoutError(null);
                }
            });

            linearLayout.addView(inputText);
        }
        return sectionView;
    }

    private View createDataSheetSection(LayoutInflater inflater, String section) {
        View sectionView = inflater.inflate(R.layout.view_data_sheet_section, null);
        ((TextView) sectionView.findViewById(R.id.section_label)).setText(section);

        LinearLayout linearLayout = sectionView.findViewById(R.id.linear_layout);
        for (String fieldName : viewModel.getInputFieldNamesBySection().get(section)) {
            View dataSheetItem = new DataSheetItemView(requireContext(), viewModel.getInputFieldModels().get(fieldName));
            linearLayout.addView(dataSheetItem);
        }
        return sectionView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.requestAllDropDownOptionsFromRepository();
    }
}
