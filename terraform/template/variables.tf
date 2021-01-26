variable "grafana_folder_name" {
  description = "(Optional) Override the name of the Grafana Folder to put Dashboard in."
  type        = string
  default     = ""
}

variable "grafana_use_existing_folder" {
  description = "(Optional) ID to an existing folder in Grafana to be used instead of creating a new one."
  type        = number
  default     = -1
}

variable "grafana_template_file" {
  description = "(Optional) Path to template for dashboard, override to provide a custom dashboard template."
  type        = string
  default     = "../static/grafana/dashboard.tpl"
}

variable "grafana_create_dashboard" {
  description = "(Optional) If should create Grafana Dashboard for application."
  type        = bool
  default     = false
}

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