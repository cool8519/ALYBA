package dal.tool.analyzer.alyba.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandLineArguments {

	private boolean optionCaseSensitive = true;
	private int minNonOption = 0;
	private int maxNonOption = Integer.MAX_VALUE;
	private List<String> allowedOptions = null;
	private Set<String> options = new HashSet<String>();
	private List<String> nonOptions = new ArrayList<String>();
	
	
	public CommandLineArguments() {}
	
	public CommandLineArguments(boolean optionCaseSensitive) {
		this.optionCaseSensitive = optionCaseSensitive;
	}		
	
	public CommandLineArguments(String[] commandlineOptions) {
		this.allowedOptions = Arrays.asList(commandlineOptions);
	}

	public CommandLineArguments(String[] commandlineOptions, boolean optionCaseSensitive) {
		this.allowedOptions = Arrays.asList(commandlineOptions);
		this.optionCaseSensitive = optionCaseSensitive;
	}

	public void setNonOptionArguments(int minCount, int maxCount) {
		this.minNonOption = minCount;
		this.maxNonOption = maxCount;
	}

	public boolean isIncludeOption(String chkOption) {
		for(String option : options) {
			if(optionCaseSensitive) {
				if(option.equals(chkOption)) {
					return true;
				}
			} else {
				if(option.equalsIgnoreCase(chkOption)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isAllowedOption(String option) {
		for(String aOption : allowedOptions) {
			if(optionCaseSensitive) {
				if(aOption.equals(option)) {
					return true;
				}
			} else {
				if(aOption.equalsIgnoreCase(option)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void parse(String[] args) throws Exception {
		for(String arg : args) {
			if(arg.startsWith("-")) {
				if(isAllowedOption(arg)) {
					if(optionCaseSensitive) {
						options.add(arg);
					} else {
						options.add(arg.toLowerCase());
					}
				} else {
					throw new Exception("Invalid Option : " + arg);
				}
			} else {
				nonOptions.add(arg);
			}
		}
		
		if(nonOptions.size() < minNonOption) {
			throw new Exception("Too few arguments : minArguments=" + minNonOption);
		}
		if(nonOptions.size() > maxNonOption) {
			throw new Exception("Too man arguments : maxArguments=" + maxNonOption);
		}
	}

	public int getOptionSize() {
		return options.size();
	}
	
	public int getNonOptionSize() {
		return nonOptions.size();
	}
	
	public Set<String> getOptions() {
		return options;
	}
	
	public List<String> getNonOptions() {
		return nonOptions;
	}
	
	public String getNonOption(int idx) {
		return nonOptions.get(idx);
	}
	
}
