variable "environment" {
  description = "Name of the environment, Ex. dev, test ,stage, prod."
  type        = string
}

variable "name_prefix" {
  description = "A prefix used for naming resources."
  type        = string
}

variable "application_name" {
  description = "The name of the application -- used together with name_prefix."
  type        = string
}

variable "tags" {
  description = "A map of tags (key-value pairs) passed to resources."
  type        = map(string)
  default     = {}
}

variable "task_container_image" {
  description = "The name of the container image that should be deployed"
  type        = string
}

variable "cognito_account_id" {
  description = "The Central Cognito account to retrieve client credentials from."
  type        = string
}

variable "cognito_override_env" {
  description = "The Central Cognito account to retrieve client credentials from."
  type        = string
  default     = ""
}

variable "cognito_bucket" {
  description = "(Optional) Configure where to upload delegated cognito config. Default is vydev-delegated-cognito-staging."
  type        = string
  default     = "vydev-delegated-cognito-staging"
}