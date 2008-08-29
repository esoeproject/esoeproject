#!/usr/bin/perl -wT

use strict;

use Fcntl qw(:mode);

my $line_number = 0;

sub error {
	print "error (line $_[0]): $_[1]\n";
}

sub warning {
	print "warning (line $_[0]): $_[1]\n";
}

sub checkConfigParameter {
	my ($name,$value) = $_[0] =~ /^([^=]+)=(.*)$/;
	my ($line_number) = $_[1];
	if (!defined $name) {
		error "Invalid line: $_[0]";
		return;
	}
	if (length($value) == 0) {
		error "No value specified for $name";
		return;
	}

	$_ = $name;
	if (/^keystorePath$/) {
		my ($dev,$ino,$mode) = stat($value);
		if (!defined($ino)) { error $line_number, "Keystore $value does not exist."; return; }
		if (!S_ISDIR($mode)) { error $line_number, "Keystore $value is a directory."; return; }
	}
	elsif (/^caBundle$/) {
		my ($dev,$ino,$mode) = stat($value);
		if (!defined($ino)) { error $line_number, "CA Bundle $value does not exist."; return; }
		if (!S_ISDIR($mode)) { error $line_number, "CA Bundle $value is a directory."; return; }
	}
	elsif (/^schemaPath$/) {
		my ($dev,$ino,$mode) = stat($value);
		if (!defined($ino)) { error $line_number, "Schema path $value does not exist. (Perhaps you haven't updated the version number?)"; return; }
		if (!S_ISDIR($mode)) { error $line_number, "Schema path $value is not a directory."; return; }
	}
	elsif (/^keystorePassword$/ or /^spepKeyAlias$/ or /^spepKeyPassword$/ or /^metadataKeyAlias$/) {
		if ($value =~ /.*\s.*/) { error $line_number, "$name=$value - assigned by registration process and should not contain a space."; return; }
	}
	elsif (/^esoeIdentifier$/ or /^spepIdentifier$/ or /^metadataUrl$/) {
		if ($value !~ /^https?:\/\/[^\.]+\.[^\.]+/) { error $line_number, "$name should look like a URL - does not validate as a http or https URL."; return; }
	}
	elsif (/^serverInfo$/) {
		# No validation here yet.
	}
	elsif (/^serviceHost$/ or /^loginRedirect$/ or /^defaultUrl$/) {
		if ($value =~ /example/) { warning $line_number, "Did you forget to change $name from its default value?"; return; }
	}
	elsif (/^nodeIdentifier$/ or /^attributeConsumingServiceIndex$/ or /^assertionConsumerServiceIndex$/ or /^authzCacheIndex$/ or /^spepDaemonPort$/) {
		if ($value !~ /^\d+$/) { error $line_number, "$name must be a numeric value."; return; }
	}
	elsif (/^ipAddresses$/) {
		if ($value !~ /^(\d+\.\d+\.\d+\.\d+)(\s+\d+\.\d+\.\d+\.\d+)*$/) { error $line_number, "$name should be a space separated list of IP addresses"; return; }
	}
	elsif (/^logoutClearCookie$/) {
		if ($value !~ /\S+(\s+\S+(\s+\S+)?)?/) { error $line_number, "Invalid format for $name - see documentation"; return; }
	}
	elsif (/^usernameAttribute$/ or /^attributeNamePrefix$/ or /^attributeValueSeparator$/) {
		if ($value !~ /^\S+$/) { error $line_number, "$name has an invalid value"; return; }
	}
	elsif (/^attributeRename$/) {
		if ($value !~ /^\S+\s+\S+$/) { error $line_number, "Invalid format for $name - see documentation"; return; }
	}
	elsif (/^lazyInit$/) {
		if ($value !~ /(true|false)/i) { error $line_number, "$name must be a boolean value (true/false)"; return; }
	}
	elsif (/^lazyInitDefaultAction$/ or /^defaultPolicyDecision$/) {
		if ($value !~ /(permit|deny)/i) { error $line_number, "$name must be either 'permit' or 'deny'"; return; }
	}
	elsif (/^lazyInit-resource$/) {
		# No validation here yet.
	}
	elsif (/^ssoRedirect$/) {
		if ($value !~ /.*\/sso\?redirectURL=%s$/) { error $line_number, "$name may be incorrect - expected it to look like /spep/sso?redirectURL=%s"; return; }
	}
	elsif (/^spepTokenName$/) {
		if ($value !~ /^\S+$/) { error $line_number, "$name - expected a single token name"; return; }
	}
	elsif (/^commonDomainTokenName$/) {
		if ($value !~ /^_saml_idp$/) { warning $line_number, "$name - warning - SAML spec says this should be '_saml_idp' - please ensure you have a reason for changing it."; return; }
	}
	elsif (/^startRetryInterval$/ or /^metadataInterval$/ or /^allowedTimeSkew$/) {
		if ($value !~ /^\d+$/) { error $line_number, "$name must be a numeric value."; return; }
		if ($value > 600) { warning $line_number, "$name is set to a very long time period - did you really mean $value seconds?"; return; }
	}
	elsif (/^identifierCacheInterval$/ or /^sessionCacheInterval$/) {
		if ($value !~ /^\d+$/) { error $line_number, "$name must be a numeric value."; return; }
		if ($value < 120) { warning $line_number, "$name is set to a very short time period - did you really mean $value seconds?"; return; }
		if ($value > 14400) { warning $line_number, "$name is set to a very long time period - did you really mean $value seconds?"; return; }
	}
	elsif (/^identifierCacheTimeout$/ or /^sessionCacheTimeout$/) {
		if ($value !~ /^\d+$/) { error $line_number, "$name must be a numeric value."; return; }
		if ($value < 600) { warning $line_number, "$name is set to a very short time period - did you really mean $value seconds?"; return; }
	}
	else {
		warning $line_number, "$name - unrecognized config option."; return;
	}
}


my $filename = $ARGV[0];
print "Checking config file: $filename\n";

open FILE, "< $filename" or die $!;
while (my $line = <FILE>) {
	$line_number++;
	chomp $line;

	$line =~ /^#/ and next;
	$line =~ /^\s*$/ and next;

	checkConfigParameter($line, $line_number);
}

