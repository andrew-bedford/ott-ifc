package ottifc.ifc;

import ottifc.ott.Specification;

import java.util.Set;

public class Monitor {
    Specification _spec;
    Set<Option> _options;

    public Monitor(Specification spec, Set<Option> options) {
        _spec = spec;
        _options = options;
    }

    //Generates and outputs the monitor
    public void generate() {
        if (_options.contains(Option.IMPLICIT_FLOWS)) {
            _spec.insertIntoAllStates("pc");
        }
        _spec.insertIntoAllStates("E");
        _spec.print();
    }
}
