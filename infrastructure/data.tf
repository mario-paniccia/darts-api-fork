locals {

  auto_secret_prefix  = "auto-${var.product}-${local.env}"
  resource_group_name = "${local.prefix}-${var.env}-rg"
  key_vault_name      = "${local.prefix}-kv-${var.env}"
  sdp_key_vault_name  = "${local.prefix}-sdp-kv-${var.env}"
}

data "azurerm_client_config" "current" {}


data "azurerm_subnet" "iaas" {
  name                 = "iaas"
  resource_group_name  = "ss-${var.env}-network-rg"
  virtual_network_name = "ss-${var.env}-vnet"
}
