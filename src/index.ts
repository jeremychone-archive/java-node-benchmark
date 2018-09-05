import * as express from 'express';
import * as cluster from 'cluster';
import { readFile, writeFile, mkdirs } from 'fs-extra';
import * as fsPath from 'path';

const desc = "some very very long very very long very very long very very long very very long very very long very very long very very " +
	"long very very long very very long very very long very very long very very long very very long description";
const DATA_LINES = 10000;
const DATA_FILE = "./~data/data.txt";

class Item {
	id: number;
	name: string;
	constructor(id: number, name: string) {
		this.id = id;
		this.name = name;
	}
}

async function createDataFile() {
	let content = '';
	for (let i = 0; i < DATA_LINES; i++) {
		content += i + ',' + 'item-' + i + ',';
		content += desc + desc + desc + ' - ' + i;
		content += '\n';
	}

	await mkdirs(fsPath.dirname(DATA_FILE));
	await writeFile(DATA_FILE, content, 'utf8');
}

if (cluster.isMaster) {
	// Count the machine's CPUs
	var cpuCount = require('os').cpus().length;
	// cpuCount = 8;
	console.log(`cpuCount ${cpuCount}`);
	createDataFile();

	// Create a worker for each CPU
	for (var i = 0; i < cpuCount; i += 1) {
		cluster.fork();
	}


} else {
	const app = express();

	const LOOP_SIZE = 100;

	app.get('/', async (req, res, next) => {
		res.setHeader('Content-Type', 'text/html; charset=utf-8');
		// console.log(req.url);
		res.send(`<h1>Hello World</h1>`);
		// res.sendStatus(200);
		next();
	});


	app.get('/data', async (req, res, next) => {
		res.setHeader('Content-Type', 'text/html; charset=utf-8');

		const content = await readFile(DATA_FILE, 'utf8');

		const lines = content.split('\n');

		for (const line of lines) {
			const cells = line.split(',');
			const id = parseInt(cells[0]);
		}

		res.send(`<h1>Data Size: ${content.length}</h1>`);

		next();
	})



	app.get('/loop', async (req, res, next) => {
		res.setHeader('Content-Type', 'text/html; charset=utf-8');

		const paramIt = req.query.it;
		const loop_size = (paramIt) ? parseInt(paramIt) : LOOP_SIZE;
		let buf = "";
		for (let i = 0; i < loop_size; i++) {
			buf += 'item ';
			buf += i;
			buf += '\n';
		}

		res.send(`<h1>Loop Size: ${loop_size} Buf Size: ${buf.length}</h1>`);

		next();
	})

	app.get('/list', async (req, res, next) => {
		res.setHeader('Content-Type', 'text/html; charset=utf-8');

		const paramIt = req.query.it;
		const loop_size = (paramIt) ? parseInt(paramIt) : LOOP_SIZE;

		// create raw data
		let buf = "";
		for (let i = 0; i < loop_size; i++) {
			buf += i;
			buf += ' item-';
			buf += i;
			buf += '\n';
		}

		// split
		const lines = buf.split('\n');
		lines.pop(); // in js, last is empty line

		// create list
		const items: Item[] = [];
		for (const line of lines) {
			const vals = line.split(" ");
			const item = new Item(parseInt(vals[0]), vals[1]);
			items.push(item);
			// items.push({ id: parseInt(vals[0]), name: vals[1] });
		}
		res.send(`<h1>Loop Size: ${loop_size} Buf Size: ${items.length}</h1>`);

		next();
	})

	// // Adding the static middleware at the beginning means it wins out over any routes present
	// // (but only interferes if the file exists)
	// app.use(express.static(webDir));

	// app.use("/node_modules", express.static('node_modules'));

	app.listen(8080);
}



