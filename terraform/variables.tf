variable "image_id" {
  // No default. Must specify a value at the command-line use the -var option.
}

variable "flavor" {
  // See available flavors with: openstack flavor list
  default = "m1.medium"
}

// We cannot ourselves create a gateway to the public internet; instead, we must use the network that the Jetstream
// admins have established for us. It is called 'public'. To get its details, run: openstack network show public
variable "external_gateway_network" {
  type = "map"
  default = {
    name = "public"
    id = "4367cd20-722f-4dc2-97e8-90d98c25f12e"
  }
}

variable "volume_size" {  // In gigabytes.
  default = 300
}

// The path to the public SSH key, relative to the Terraform root module (i.e. the 'terraform' directory).
variable "public_key_path" {
  description = "The path of the SSH public key"
  default = "../ansible/files/unidata_provisioner_id_rsa.pub"
}

variable "user_data_path" {
  description = "The path to the user-data file that configures the VM via cloud-init."
  default = "../packer/user-data.yml"
}
