package es.uni_freiburg.de.cmotion.shared_ui.model;


import java.util.ArrayList;

public class SensorModel implements Comparable<SensorModel>{

    private String mName;
    private boolean mEnabled;
    private int mSamplingRate = 50; // TODO use constant

    private ArrayList<String> mAvailableOnPlatforms = new ArrayList<>();

    public SensorModel(String name) {
        this(name, false);
    }

    public SensorModel(String name, boolean enabled) {
        mName = name;
        mEnabled = enabled;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    @Override
    public int compareTo(SensorModel another) {
        return getEasyName().compareTo(another.getEasyName());
    }

    public void setSamplingRate(int samplingRate) {
        this.mSamplingRate = samplingRate;
    }

    public int getSamplingRate() {
        return mSamplingRate;
    }

    public ArrayList<String> getAvailablePlatforms() {
        return mAvailableOnPlatforms;
    }

    public void addAvailablePlatform(String availableOnPlatform) {
        if(!mAvailableOnPlatforms.contains(availableOnPlatform))
            this.mAvailableOnPlatforms.add(availableOnPlatform);
    }

    public String getEasyName() {
        return  getName().contains(".") ?
                getName().substring(getName().lastIndexOf(".")+1).replace("_", " ") :
                getName();
    }
}
