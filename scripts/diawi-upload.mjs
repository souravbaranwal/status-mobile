#!/usr/bin/env node

import { upload } from 'diawi-nodejs-uploader';
import { basename } from 'node:path';

const diawiToken = process.env.DIAWI_TOKEN;
const targetFile = process.argv[2]
const comment = process.argv[3]

if (diawiToken === undefined) {
  console.error('No DIAWI_TOKEN env var provided!')
  process.exit(1)
}
if (targetFile === undefined) {
  console.error('No file path provided!')
  process.exit(1)
}

process.stderr.write(`Uploading: ${targetFile}\n`)
 
const result = await upload({
  file: targetFile,
  token: diawiToken,
  comment: comment || basename(targetFile),
})

console.log(result)
if (result.message != 'Ok') {
  process.exit(1)
}
