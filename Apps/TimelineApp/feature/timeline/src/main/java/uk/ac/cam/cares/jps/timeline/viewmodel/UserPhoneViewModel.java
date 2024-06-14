package uk.ac.cam.cares.jps.timeline.viewmodel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import uk.ac.cam.cares.jps.login.LoginRepository;
import uk.ac.cam.cares.jps.utils.RepositoryCallback;
import uk.ac.cam.cares.jps.sensor.UserPhoneRepository;

@HiltViewModel
public class UserPhoneViewModel extends ViewModel {
    UserPhoneRepository userPhoneRepository;
    LoginRepository loginRepository;
    private final MutableLiveData<Throwable> _error = new MutableLiveData<>();
    private final LiveData<Throwable> error = _error;

    @Inject
    public UserPhoneViewModel(UserPhoneRepository userPhoneRepository, LoginRepository loginRepository) {
        this.userPhoneRepository = userPhoneRepository;
        this.loginRepository = loginRepository;
    }

    public void registerPhoneToUser() {
        userPhoneRepository.registerAppToUser(new RepositoryCallback<>() {
            @Override
            public void onSuccess(Boolean result) {
                _error.setValue(null);
            }

            @Override
            public void onFailure(Throwable error) {
                _error.setValue(error);
            }
        });
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public MaterialAlertDialogBuilder getSessionExpiredDialog(Fragment fragment) {
        return loginRepository.getSessionExpiredDialog(fragment);
    }
}
