package esthesis.common.agent.dto;

import esthesis.common.util.EsthesisCommonConstants;
import esthesis.common.util.EsthesisCommonConstants.Device.Capability;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class AgentRegistrationRequest {

	// The hardware ID of the device. It can only contain alphanumeric values, hyphens, and underscores.
	@NotBlank
	@Length(min = 3, max = 512)
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Hardware ID must contain only alphanumeric characters, hyphens, and underscores.")
	private String hardwareId;

	// Comma-separated list of tag names.
	private String tags;

	// The type of the device being registered.
	@NotNull
	private EsthesisCommonConstants.Device.Type type;

	// The optional registration secret, when the platform operates in that mode.
	private String registrationSecret;

	// A comma-separated list of key-value-type tuples in the form of:
	// key1=val1;type1,key2=val2;type2,etc.
	// The type of the attribute is optional and if not defined the system will try to determine
	// what is the most appropriate type to use. If the type is defined, it must be one of the
	// values provided by AppConstants.Device.Attribute.Type.
	private String attributes;

	// Capabilities supported by this device.
	@Singular
	private List<Capability> capabilities;
}
