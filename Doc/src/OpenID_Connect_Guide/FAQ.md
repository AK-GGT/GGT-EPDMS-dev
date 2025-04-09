# OpenID FAQ

## Can a token obtained from and identity provider used as a Bearer token?

Yes<sup>*</sup>.

\* please read [ODIC_spec.md](./OIDC_spec) and [OIDC_config.md](./OIDC_config) first. 

## The discovery document is missing mandatory fields. what can do about that?

1. Spoof the discovery document.
2. Add missing fields.
3. Host modified discovery document publicly and point to it in the soda4LCA.properties.

## How to debug a soda4LCA node that uses a real identity provider?

You have few options. pick one:

1. Ask identity provider admin to add a new domain (under your control) to the list of allowed base urls of the client application. 
2. Update the dns records of the node's domain to point your machine.
3. Use ngrok to redirect the traffic from remote machine to your machine.

### on dev machine

```bash
$ ngrok tcp 127.0.0.1:8080
```

### on remote machine

![ngrok-remote](https://user-images.githubusercontent.com/1815268/156939565-97eca1e8-99ff-4039-a6f1-b06d9e8e5bd9.png)


## There is a change password/update profile/assign roles button, can i use that?

No. these will not work when the user is logged with OpenID.

To change any setting, contact the administrator of the identity provider.

## A new datastock has been created, can soda4LCA allow OpenID users who have a specific role access to it?

Yes. please check roleMapping options in you soda4LCA.properties.

## Identity provider doesn't provide all/any roles. I need to contact another remote server, can soda4LCA do that?

Yes<sup>*</sup>.

\* please read [ODIC_spec.md](./OIDC_spec) and [OIDC_config.md](./OIDC_config) first.