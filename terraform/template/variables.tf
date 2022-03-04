variable "name_prefix" {
  description = "A prefix used for naming resources."
  type        = string
}

variable "application_name" {
  description = "The name of the application -- used together with name_prefix."
  type        = string
}

variable "application_image_tag" {
  type = string

  default = "latest"
}

variable "environment" {
  description = "Name of the environment, Ex. dev, test ,stage, prod."
  type        = string
}