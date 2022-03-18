terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "4.6.0"
    }
    grafana = {
      source  = "grafana/grafana"
      version = "1.14.0"
    }
  }
  required_version = "1.0.0"
}
