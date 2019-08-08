## Generating flows

To generate a new flow, run:

```
./flow.sh <identifier> <ABI> <steps ...>
```

- identifier - all-lowercase name for this flow, for example "freeze" or 
  "suggestions".
- ABI - Path to ABI or address to pull from Etherscan
- steps ... - The remaining arguments are one step each for the new flow. 
  Each step should be either the name of a function, or "-" for a text-only step.

For example:

```
./flow.sh bundler ../EgoCoin.abi - addBundler - approve -
```

## Generating function screens


